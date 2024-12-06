package com.example.examplesgoogleapi.googleapi.legacy

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
//import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File


/**
 * refs :
 * - https://proandroiddev.com/android-kotlin-jetpack-compose-interacting-with-google-drive-api-v3-2023-the-complete-b8bc1bdbb13b
 */
class GoogleDriveServiceHelper2(val activity: Activity, fragment: Fragment) {


	fun getGoogleSignInClient(): GoogleSignInClient {
		val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestEmail()
			.requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE))
			.build()

		return GoogleSignIn.getClient(activity, signInOptions)
	}


	val startForResult = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
		if (result.resultCode == Activity.RESULT_OK) {
			val intent = result.data
			if (result.data != null) {
				val task: Task<GoogleSignInAccount> =
					GoogleSignIn.getSignedInAccountFromIntent(intent)

				/**
				 * handle [task] result
				 */
			} else {
				Toast.makeText(activity, "Google Login Error!", Toast.LENGTH_LONG).show()
			}
		}
	}


	fun getGoogleDriveInstance(context: Context): Drive? {

		return GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->

			// get credentials
			val credential = GoogleAccountCredential.usingOAuth2(
				context, listOf(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
			)
			credential.selectedAccount = googleAccount.account!!

			// get Drive Instance
			/*
			// TODO:
			Drive
				.Builder(
					AndroidHttp.newCompatibleTransport(),
					JacksonFactory.getDefaultInstance(),
					credential
				)
				.setApplicationName(context.getString(R.string.app_name))
				.build()
			 */
			null
		}
	}


	fun createFolder(drive: Drive) {
		// Define a Folder
		val gFolder = com.google.api.services.drive.model.File()
		// Set file name and MIME
		gFolder.name = "My Cool Folder Name"
		gFolder.mimeType = "application/vnd.google-apps.folder"

		// You can also specify where to create the new Google folder
		// passing a parent Folder Id
		val parents: MutableList<String> = ArrayList(1)
		parents.add("your_parent_folder_id_here")
		gFolder.parents = parents
		drive.Files().create(gFolder).setFields("id").execute()
	}


	fun createFiles(drive: Drive, files: List<File>) {
		for (file in files) createFile(drive, file)
	}

	fun createFile(drive: Drive, file: File) {
		val gfile = com.google.api.services.drive.model.File()

		val fileContent = FileContent("your_mime", file)
		gfile.name = file.name

		val parents: MutableList<String> = ArrayList(1)
		parents.add("folder_id") // Here you need to get the parent folder id

		gfile.parents = parents

		drive.Files().create(gfile, fileContent).setFields("id").execute()
	}


}