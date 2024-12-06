@file:Suppress("FunctionName", "PrivatePropertyName")

package com.example.examplesgoogleapi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.examplesgoogleapi.googleapi.GoogleServiceHelper
import com.example.examplesgoogleapi.googleapi.legacy.DriveServiceHelper3
import com.example.examplesgoogleapi.googleapi.legacy.GoogleDriveServiceHelper2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes


/**
 *
 */
class FragmentExample : Fragment(R.layout.page_example) {


	/* View */

	private lateinit var btnBackupNow	: Button



	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		mFileTitleEditText	= view.findViewById(R.id.settings_file_title_et);
		mDocContentEditText	= view.findViewById(R.id.settings_doc_content_et);

		btnBackupNow	= view.findViewById(R.id.settings_btn_backupNow)


		/* UI listeners */

		btnBackupNow.setOnClickListener {
			Log.d(TAG, "Requesting sign-in")
			googleDriveServiceHelper.getDriveServiceHelper().authorizeGoogleDrive(launcherDrive)
		}

		view.findViewById<Button>(R.id.settings_btn_open	).setOnClickListener { v -> openFilePicker() }
		view.findViewById<Button>(R.id.settings_btn_create	).setOnClickListener { v -> createFile() }
		view.findViewById<Button>(R.id.settings_btn_save	).setOnClickListener { v -> saveFile() }
		view.findViewById<Button>(R.id.settings_btn_query	).setOnClickListener { v -> query() }

		view.findViewById<Button>(R.id.settings_btn_clear)?.setOnClickListener { v ->
			Log.d(TAG, "Requesting clear credential")
			googleDriveServiceHelper.clearCredential()

		}

		view.findViewById<Button>(R.id.settings_btn_createPassword)?.setOnClickListener { v ->
			Log.d(TAG, "Requesting create credential with password")
			googleDriveServiceHelper.registerPassword("user", "12345678")

		}

		view.findViewById<Button>(R.id.settings_btn_createCredential)?.setOnClickListener { v ->
			Log.d(TAG, "Requesting create credential")

		}

		view.findViewById<Button>(R.id.settings_btn_signin)?.setOnClickListener { v ->
			Log.d(TAG, "Requesting signIn flow")
			googleDriveServiceHelper.signInFlow()
		}

	}


	/* Google Drive */

	private val googleDriveServiceHelper by lazy {
		GoogleServiceHelper(requireActivity().lifecycleScope, requireContext(), requireActivity(), this)
	}

	private val launcherDrive = registerForActivityResult(
		ActivityResultContracts.StartIntentSenderForResult()
	) { result: ActivityResult ->
		Log.d(TAG, "result $result")
		result.data?.apply {
			Log.d(TAG, "${this.extras?.keySet()?.joinToString { it }}")
			Log.d(TAG, "${this.extras?.get("authorization_result")}")
			Log.d(TAG, "${this.extras?.get("status")}")
		}
		when(result.resultCode) {
			Activity.RESULT_OK -> {
				Log.d(TAG, "authorized")
			} else -> {
				Log.e(TAG, "Authorization UI returned error: code ${result.resultCode}, data ${result.data}.")
			}
		}
	}


	/* Google Drive (github) */


	private var mFileTitleEditText: EditText? = null
	private var mDocContentEditText: EditText? = null




	/*
	 * Deprecated usages
	 */

	private val REQUEST_CODE_SIGN_IN		= 1
	private val REQUEST_CODE_OPEN_DOCUMENT	= 2

	private var mDriveServiceHelper: DriveServiceHelper3? = null
	private var mOpenFileId: String? = null

	private val googleDriveServiceHelper2 by lazy {
		GoogleDriveServiceHelper2(requireActivity(), this)
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
		super.onActivityResult(requestCode, resultCode, resultData)
		Log.d(TAG, "on activity result req $requestCode, res $resultData")
		when (requestCode) {
			REQUEST_CODE_SIGN_IN -> {/* done */}
			REQUEST_CODE_OPEN_DOCUMENT -> if (resultCode == Activity.RESULT_OK && resultData != null) {
				val uri = resultData.data
				if (uri != null) {
					openFileFromFilePicker(uri)
				}
			}
		}
	}

	/**
	 * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
	 * ( in StackOverflow : `driveSetUp()` )
	 */
	private fun handleSignInResult(result: Intent) {
		GoogleSignIn.getSignedInAccountFromIntent(result)
			.addOnSuccessListener { googleAccount: GoogleSignInAccount ->
				Log.d(TAG, "Signed in as " + googleAccount.email)
				// Use the authenticated account to sign in to the Drive service.
				val credential =
					GoogleAccountCredential.usingOAuth2(
						requireContext(), setOf(DriveScopes.DRIVE_FILE)
					)
				credential.setSelectedAccount(googleAccount.account)
				val googleDriveService =
					Drive.Builder(
						NetHttpTransport(),
						GsonFactory(),
						credential
					)
						.setApplicationName("Drive API Migration")
						.build()

				// The DriveServiceHelper encapsulates all REST API and SAF functionality.
				// Its instantiation is required before handling any onClick actions.
				mDriveServiceHelper = DriveServiceHelper3(googleDriveService)
			}
			.addOnFailureListener { exception: Exception? ->
				Log.e(
					TAG,
					"Unable to sign in.",
					exception
				)
			}
	}



	/**
	 * Opens the Storage Access Framework file picker using [.REQUEST_CODE_OPEN_DOCUMENT].
	 */
	private fun openFilePicker() {
		mDriveServiceHelper?.let { driveServiceHelper ->
			Log.d(TAG, "Opening file picker.")

			val pickerIntent: Intent = driveServiceHelper.createFilePickerIntent()

			// The result of the SAF Intent is handled in onActivityResult.
			startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT)
		}
	}

	/**
	 * Opens a file from its `uri` returned from the Storage Access Framework file picker
	 * initiated by [.openFilePicker].
	 */
	private fun openFileFromFilePicker(uri: Uri) {
		mDriveServiceHelper?.let { driveServiceHelper ->
			Log.d(TAG, "Opening " + uri.path)

			driveServiceHelper.openFileUsingStorageAccessFramework(requireActivity().contentResolver, uri)
				.addOnSuccessListener { nameAndContent ->
					val name: String = nameAndContent.first
					val content: String = nameAndContent.second

					mFileTitleEditText?.setText(name)
					mDocContentEditText?.setText(content)

					// Files opened through SAF cannot be modified.
					setReadOnlyMode()
				}
				.addOnFailureListener { exception ->
					Log.e(
						TAG,
						"Unable to open file from picker.",
						exception
					)
				}
		}
	}

	/**
	 * Creates a new file via the Drive REST API.
	 */
	private fun createFile() {

		//mDriveServiceHelper?.createFile()
		googleDriveServiceHelper.getDriveServiceHelper().createFile()

		/*
		mDriveServiceHelper?.let { driveServiceHelper ->
			Log.d(TAG, "Creating a file.")

			driveServiceHelper.createFile()
				.addOnSuccessListener { fileId -> readFile(fileId) }
				.addOnFailureListener { exception ->
					Log.e(
						TAG,
						"Couldn't create file.",
						exception
					)
				}
		}
		 */
	}


	/**
	 * Retrieves the title and content of a file identified by `fileId` and populates the UI.
	 */
	private fun readFile(fileId: String) {
		mDriveServiceHelper?.let { driveServiceHelper ->
			Log.d(TAG, "Reading file $fileId")

			driveServiceHelper.readFile(fileId)
				.addOnSuccessListener { nameAndContent ->
					val name: String = nameAndContent.first
					val content: String = nameAndContent.second

					mFileTitleEditText?.setText(name)
					mDocContentEditText?.setText(content)
					setReadWriteMode(fileId)
				}
				.addOnFailureListener { exception ->
					Log.e(
						TAG,
						"Couldn't read file.",
						exception
					)
				}
		}
	}

	/**
	 * Saves the currently opened file created via [.createFile] if one exists.
	 */
	private fun saveFile() {
		mDriveServiceHelper?.let { driveServiceHelper ->
			if (mOpenFileId != null) {
				Log.d(TAG, "Saving $mOpenFileId")

				val fileName:		String = mFileTitleEditText?.getText().toString()
				val fileContent:	String = mDocContentEditText?.getText().toString()

				driveServiceHelper.saveFile(mOpenFileId, fileName, fileContent)
					.addOnFailureListener { exception ->
						Log.e(
							TAG,
							"Unable to save file via REST.",
							exception
						)
					}
			} }
	}

	/**
	 * Queries the Drive REST API for files visible to this app and lists them in the content view.
	 */
	private fun query() {
		mDriveServiceHelper?.let { driveServiceHelper ->
			Log.d(TAG, "Querying for files.")

			driveServiceHelper.queryFiles()
				.addOnSuccessListener { fileList ->
					val builder = StringBuilder()
					for (file in fileList.getFiles()) {
						builder.append(file.getName()).append("\n")
					}
					val fileNames = builder.toString()

					mFileTitleEditText?.setText("File List")
					mDocContentEditText?.setText(fileNames)
					setReadOnlyMode()
				}
				.addOnFailureListener { exception ->
					Log.e(
						TAG,
						"Unable to query files.",
						exception
					)
				}
		}
	}

	/**
	 * Updates the UI to read-only mode.
	 */
	private fun setReadOnlyMode() {
		mFileTitleEditText?.setEnabled(false)
		mDocContentEditText?.setEnabled(false)
		mOpenFileId = null
	}

	/**
	 * Updates the UI to read/write mode on the document identified by `fileId`.
	 */
	private fun setReadWriteMode(fileId: String) {
		mFileTitleEditText?.setEnabled(true)
		mDocContentEditText?.setEnabled(true)
		mOpenFileId = fileId
	}


	/*
	 * See official docs: https://developers.google.com/drive/api/guides/manage-uploads
	 */



	companion object {

		private const val TAG = "Settings"

    }
}