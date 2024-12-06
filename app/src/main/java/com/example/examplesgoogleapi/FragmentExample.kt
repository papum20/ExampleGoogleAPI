@file:Suppress("FunctionName", "PrivatePropertyName")

package com.example.examplesgoogleapi

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.examplesgoogleapi.googleapi.GoogleServiceHelper
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


	private fun openFilePicker() {}
	private fun createFile() {}
	private fun saveFile() {}
	private fun query() {}


	companion object {

		private const val TAG = "Settings"

    }
}