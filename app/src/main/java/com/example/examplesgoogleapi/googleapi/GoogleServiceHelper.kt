@file:Suppress("PrivatePropertyName")

package com.example.examplesgoogleapi.googleapi

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.Fragment
import com.example.examplesgoogleapi.R
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import kotlin.jvm.Throws


/**
 * Use an activity based context to ensure that the system credential selector ui is launched
 * within the same activity stack and to avoid undefined system UI launching behavior, in
 * [CredentialManager] functions.
 */
class GoogleServiceHelper(
	private val coroutineScope: CoroutineScope,
	private val context: Context,
	private val activity: Activity,
	private val fragment: Fragment
) {

	class NullDriveException(message: String) : Exception(message)


	private var mDriveService: GoogleDriveServiceHelper? = null

	private var requestedScopes: List<Scope> = listOf(
		Scope(DriveScopes.DRIVE_APPDATA),
		Scope(DriveScopes.DRIVE_FILE)
	)

	private val APPLICATION_NAME:	String = "HomeCook's Companion Drive"
	private val CLIENT_ID_WEB:		String = context.getString(R.string.oauth2_client_id_web)



	@Throws(NullDriveException::class)
	fun getDriveServiceHelper(): GoogleDriveServiceHelper =
		try {
			mDriveService!!
		} catch (e: NullPointerException) {
			Log.e(TAG, "mDriveService is null")
			throw NullDriveException("mDriveService is null")
		}


	private fun initDriveService(credentialId: String) {

		Log.d(TAG, "credentialId $credentialId")

		/*
		val credential =
			GoogleAccountCredential.usingOAuth2(
				context, setOf(DriveScopes.DRIVE_FILE)
			)
		credential.setSelectedAccount(googleAccount.account)
		val mDriveService =
			Drive.Builder(
				AndroidHttp.newCompatibleTransport(),
				GsonFactory(),
				credential
			)
				.setApplicationName("Drive API Migration")
				.build()
		 */
		// https://stackoverflow.com/questions/73583035/obtaining-googlecredentials-from-one-tap-signin
		// https://stackoverflow.com/a/58421822/20607105
		// set exponential backoff policy
		val credentials = GoogleAccountCredential.usingOAuth2(context,
			listOf(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE) )
			.setBackOff(ExponentialBackOff())
			.setSelectedAccount(
				Account(credentialId, activity.application.packageName)
			)

		Log.d(TAG, "got credentials")

		// https://developers.google.com/drive/api/guides/manage-uploads
		// Load pre-authorized user credentials from the environment.
		// TODO(developer) - See https://developers.google.com/identity for
		// guides on implementing OAuth2 for your application.

		/*
		val credentialsOauth = GoogleCredentials.getApplicationDefault()
			.createScoped(listOf(DriveScopes.DRIVE_FILE))
		val requestInitializer = HttpCredentialsAdapter(credentialsOauth)

		Log.d(TAG, "got credentialsOauth")
		*/

		// Build a new authorized API client service.
		Drive.Builder(
			// instead of `AndroidHttp.newCompatibleTransport()`, after Gingerbread
			NetHttpTransport(),
			GsonFactory.getDefaultInstance(),
			credentials //requestInitializer
		)
			.setApplicationName(APPLICATION_NAME)
			.build().let {
				mDriveService = GoogleDriveServiceHelper(it, coroutineScope, activity)
			}

		Log.d(TAG, "got Drive Service")

	}



	/**
	 * Directory to store authorization tokens for this application.
	 */
	val TOKENS_DIRECTORY_PATH: String = "tokens"
	
	val CREDENTIALS_FILE_PATH: String = "/credentials.json"

	/**
	 * Creates an authorized Credential object.
	 *
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	/*@Throws(IOException::class)
	private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
		// Load client secrets.
		val `in`: InputStream =
			GoogleDriveServiceHelper::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
				?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
		val clientSecrets =
			GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

		// Build flow and trigger user authorization request.
		val flow = GoogleAuthorizationCodeFlow.Builder(
			HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
		)
			.setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
			.setAccessType("offline")
			.build()
		val receiver: LocalServerReceiver = Builder().setPort(8888).build()
		val credential: Credential = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
		//returns an authorized Credential object.
		return credential
	}*/


	/*
	 * Credentials
	 */

	// https://developer.android.com/identity/sign-in/credential-manager-siwg
	val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
		// true gets already authorized account by default, false lets choose
		.setFilterByAuthorizedAccounts(false)
		.setServerClientId(CLIENT_ID_WEB)
		// automatic sign-in for returning user
		//.setAutoSelectEnabled(true)
		//.setNonce(<nonce string to use when generating a Google ID token>)
		.build()

	// deprecated
	val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(CLIENT_ID_WEB)
		//.setNonce(<nonce string to use when generating a Google ID token>)
		.build()

	/**
	 * Authentication
	 * ref: https://developer.android.com/identity/sign-in/credential-manager-siwg
 	 */
	fun signInFlow() {

		// https://developer.android.com/identity/sign-in/credential-manager-siwg
		val request = GetCredentialRequest.Builder()
			//.addCredentialOption(signInWithGoogleOption) //(googleIdOption)
			// or...
			.addCredentialOption(googleIdOption) //(googleIdOption)
			.build()

		fun getGoogleIdOption(): GetGoogleIdOption {

			val rawNonce	= UUID.randomUUID().toString()
			val bytes		= rawNonce.toByteArray()
			val digest		= MessageDigest.getInstance("SHA-256").digest(bytes)
			val hashedNonce	= digest.fold("") { str, it ->
				str + "%02x".format(it)
			}

			return GetGoogleIdOption.Builder()
				// true - check if the user has any accounts that have previously been used to sign in to the app
				.setFilterByAuthorizedAccounts(false)
				.setServerClientId(CLIENT_ID_WEB)
				// true - Enable automatic sign-in for returning users
				.setAutoSelectEnabled(true)
				.setNonce(hashedNonce)
				.build()
		}

		val credentialManager = CredentialManager.create(context)

		val googleSignRequest: GetCredentialRequest = GetCredentialRequest.Builder()
			.addCredentialOption(getGoogleIdOption())
			.build()

		// Retrieves the user's saved password for your app from their password provider.
		val getCredRequest = GetCredentialRequest.Builder()
			.addCredentialOption( GetPasswordOption() )
			.build()


		coroutineScope.launch {
			try {
				val result = credentialManager.getCredential(
					request = googleSignRequest,
					context = activity,
				)
				Log.d(TAG, "Got credential result")
				handleSignIn(result)
			} catch (e: GetCredentialException) {
				Log.e(TAG, "Error getting credential", e)
				//handleFailure(e)
			} catch (e: NoCredentialException) {
				Log.e(TAG, "No credentials", e)
				// signup
			}
		}

	}

	/**
	 * Handle the successfully returned credential.
	 */
	private suspend fun handleSignIn(result: GetCredentialResponse) {
		val credential = result.credential
		Log.d(TAG, "signIn result: ${result.credential.type}, ${result.credential.data}")

		when (credential) {

			// Passkey credential
			is PublicKeyCredential -> {
				Log.e("TAG", "Unexpected type of credential")
			}
			is PasswordCredential -> {
				// Send ID and password to your server to validate and authenticate.
				val username = credential.id
				val password = credential.password

				initDriveService(username)
			}

			// GoogleIdToken credential

			/*
				4. The type for CustomCredential should be equal to the value of GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.
			 		Convert the object into a GoogleIdTokenCredential using the
			 		GoogleIdTokenCredential.createFrom method.
			 	5. If the conversion succeeds, extract the GoogleIdTokenCredential ID,
			 		validate it, and authenticate the credential on your server.
				6. If the conversion fails with a GoogleIdTokenParsingException, then you may need
					to update your Sign in with Google library version.
			 */
			is CustomCredential -> {
				if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
					try {
						// Use googleIdTokenCredential and extract the ID to validate and
						// authenticate on your server.
						val googleIdTokenCredential = GoogleIdTokenCredential
							.createFrom(credential.data)

						Log.d(TAG, googleIdTokenCredential.id)
						initDriveService(googleIdTokenCredential.id)

					} catch (e: GoogleIdTokenParsingException) {
						Log.e(TAG, "Received an invalid google id token response", e)
					}  catch (e: Exception) {
						e.printStackTrace()
						Log.e(TAG, "Unexpected error")
					}
				} else {
					Log.e(TAG, "Unexpected type of credential")
				}
			}

			else -> {
				Log.e(TAG, "Unexpected type of credential")
			}
		}
	}

	/**
	 * Handle the successfully returned credential.
	 * Use Firebase backend.
	 */
	private suspend fun handleSignIn_firebase(result: GetCredentialResponse) {
		val credential = result.credential
		Log.d(TAG, "signIn result: ${result.credential.type}, ${result.credential.data}")

		when (credential) {

			// Passkey credential
			is PublicKeyCredential -> {
				Log.e("TAG", "Unexpected type of credential")
			}
			is PasswordCredential -> {
				// Send ID and password to your server to validate and authenticate.
				val username = credential.id
				val password = credential.password

				initDriveService(username)
			}

			// GoogleIdToken credential

			/*
				4. The type for CustomCredential should be equal to the value of GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.
			 		Convert the object into a GoogleIdTokenCredential using the
			 		GoogleIdTokenCredential.createFrom method.
			 	5. If the conversion succeeds, extract the GoogleIdTokenCredential ID,
			 		validate it, and authenticate the credential on your server.
				6. If the conversion fails with a GoogleIdTokenParsingException, then you may need
					to update your Sign in with Google library version.
			 */
			is CustomCredential -> {
				if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
					try {
						// Use googleIdTokenCredential and extract the ID to validate and
						// authenticate on your server.
						val googleIdTokenCredential = GoogleIdTokenCredential
							.createFrom(credential.data)

						// You can use the members of googleIdTokenCredential directly for UX
						// purposes, but don't use them to store or control access to user
						// data. For that you first need to validate the token:
						// pass googleIdTokenCredential.getIdToken() to the backend server.
						//val verifier: GoogleIdTokenVerifier = ... // see validation instructions
						//val idToken: GoogleIdToken = verifier.verify(idTokenString)
						// To get a stable account identifier (e.g. for storing user data),
						// use the subject ID:
						//idToken.getPayload().getSubject()

						val authCredential = GoogleAuthProvider.getCredential(
							googleIdTokenCredential.idToken, null )
						Firebase.auth.signInWithCredential(authCredential).await().let {
							it.additionalUserInfo?.let { u ->
								Log.d(
									TAG, "Firebase authenticated credential: username ${u.username}, " +
										"isNewUser ${u.isNewUser}, providerId ${u.providerId}; provider profile ${u.profile}" )
							}

							// `profile` field is used for google information, in case of
							// sign-in with Google to the Firebase DB.
							// `email` field, instead, is just the user's email on Firebase.
							it.additionalUserInfo?.profile
								?.get(AUTHENTICATION_PROFILE_KEY_GOOGLE_EMAIL)?.let { googleEmail ->
									initDriveService(googleEmail.toString())
								}
						}


					} catch (e: GoogleIdTokenParsingException) {
						Log.e(TAG, "Received an invalid google id token response", e)
					}  catch (e: Exception) {
						e.printStackTrace()
						Log.e(TAG, "Unexpected error")
					}
				} else {
					Log.e(TAG, "Unexpected type of credential")
				}
			}

			else -> {
				Log.e(TAG, "Unexpected type of credential")
			}
		}
	}


	fun registerPassword(username: String, password: String) {
		val credentialManager		= CredentialManager.create(activity)
		val createPasswordRequest	= CreatePasswordRequest(id = username, password = password)

		coroutineScope.launch {
			try {
				val result = credentialManager.createCredential(activity, createPasswordRequest)
				Log.d(TAG, "Created password. $result")
				//handleRegisterPasswordResult(result)
			} catch (e: CreateCredentialException) {
				Log.e(TAG, "Error getting credential", e)
				//handleFailure(e)
			}
		}
	}

	fun clearCredential() {
		val credentialManager		= CredentialManager.create(activity)
		val clearCredentialRequest	= ClearCredentialStateRequest(
			ClearCredentialStateRequest.TYPE_CLEAR_CREDENTIAL_STATE )

		coroutineScope.launch {
			try {
				val result = credentialManager.clearCredentialState(clearCredentialRequest)

				Log.d(TAG, "Cleared credential. $result")
				//handleRegisterPasswordResult(result)
			} catch (e: CreateCredentialException) {
				Log.e(TAG, "Error clearing credential", e)
				//handleFailure(e)
			}
		}
	}



	companion object {

		private const val TAG = "sGoogle"

		// key for email in `profile` field of Firebase authenticated user
		private const val AUTHENTICATION_PROFILE_KEY_GOOGLE_EMAIL = "email"

	}

}