@file:Suppress("PrivatePropertyName")

package com.example.examplesgoogleapi.googleapi

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


/**
 * Use an activity based context to ensure that the system credential selector ui is launched
 * within the same activity stack and to avoid undefined system UI launching behavior, in
 * [CredentialManager] functions.
 */
class GoogleDriveServiceHelper(
	private val mDriveService: Drive,
	private val coroutineScope: CoroutineScope,
	private val activity: Activity,
) {

	private var requestedScopes: List<Scope> = listOf(
		Scope(DriveScopes.DRIVE_APPDATA),
		Scope(DriveScopes.DRIVE_FILE)
	)


	/* Authorization, for Google Drive */

	fun authorizeGoogleDrive(launcherDrive: ActivityResultLauncher<IntentSenderRequest>) {
		val authorizationRequest = AuthorizationRequest.builder()
			.setRequestedScopes(requestedScopes)
			.build()
		Identity.getAuthorizationClient(activity)
			.authorize(authorizationRequest)
			.addOnSuccessListener { authorizationResult ->
				authorizationResult.toGoogleSignInAccount()?.apply {
					Log.d(TAG, "Authorization success: $id, $email, $account, $displayName, $familyName, $givenName, ${this.grantedScopes}, ${this.idToken}, ${this.isExpired}, ${this.photoUrl}, ${this.requestedScopes}, ${this.serverAuthCode}")
				}
				if (authorizationResult.hasResolution()) {
					// Access needs to be granted by the user
					authorizationResult.pendingIntent?.intentSender?.let { intentSender ->
						launcherDrive.launch(
							IntentSenderRequest.Builder(intentSender).build()
						)
						Log.d(TAG, "launched authorize UI")
					} ?: run {
						Log.e(TAG, "Couldn't start authorization UI because intent is null.")
					}
				} else {
					// Access already granted, continue with user action
					//saveToDriveAppFolder(authorizationResult)
					Log.d(TAG, "already authorized: $authorizationResult")
				}
			}
			.addOnFailureListener { e -> Log.e(TAG, "Failed to authorize", e) }
	}


	/*
	fun createFile(): Task<String> {
		return Tasks.call(mExecutor) {
			val metadata =
				File()
					.setParents(listOf("root"))
					.setMimeType("text/plain")
					.setName("Untitled file")
			val googleFile =
				mDriveService.files().create(metadata).execute()
					?: throw IOException("Null result when requesting file creation.")
			googleFile.id
		}
	}
	 */

	fun createFile() {
		val metadata = File()
			.setParents(listOf("root"))
			.setMimeType("text/plain")
			.setName("Titled file")
		mDriveService.let {
			Log.d(TAG, "Creating file")
			coroutineScope.launch {
				withContext(Dispatchers.IO) {
					val googleFile = it.files().create(metadata).execute()
								?: throw IOException("Null result when requesting file creation.")
					googleFile.id
				}
			}
		}
	}




	companion object {

		private const val TAG = "sDrive"

	}

}