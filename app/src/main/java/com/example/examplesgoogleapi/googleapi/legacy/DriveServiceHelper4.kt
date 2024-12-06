package com.example.examplesgoogleapi.googleapi.legacy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader


class DriveServiceHelper4(val fragment: Fragment, val fileText: TextView, val context: Context, val activity: Activity) {

	var launcherOpen = fragment.registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			result.data?.data?.let { currentUri ->
				try {
					readFileContent(currentUri)?.let { content ->
						fileText.text = content
					}
				} catch (e: IOException) {
					// Handle error here
				}
			}
		}
	}


	var launcherSave = fragment.registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			result.data?.data?.let { currentUri ->
				try {
					writeFileContent(currentUri)
				} catch (e: IOException) {
					// Handle error here
				}
			}
		}
	}


	var launcherCreate = fragment.registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			result.data?.let {
				fileText.text = ""
			}
		}
	}


	fun newFile(view: View?) =
		launcherCreate.launch(
			Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
				addCategory(Intent.CATEGORY_OPENABLE)
				setType("text/plain")
				putExtra(Intent.EXTRA_TITLE, "newfile.txt")
			}
		)


	fun saveFile(view: View?) {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
		intent.addCategory(Intent.CATEGORY_OPENABLE)
		intent.setType("text/plain")

		launcherSave.launch(intent)
	}


	@Throws(IOException::class)
	private fun writeFileContent(uri: Uri) {
		try {
			context.contentResolver.openFileDescriptor(uri, "w")?.let { fd ->

				val fileOutputStream = FileOutputStream( fd.fileDescriptor )

				val textContent: String = fileText.getText().toString()
				fileOutputStream.write(textContent.toByteArray())

				fileOutputStream.close()
				fd.close()
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	fun openFile(view: View?) =
		launcherOpen.launch(
			Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
				addCategory(Intent.CATEGORY_OPENABLE)
				setType("text/plain")
			}
		)


	@Throws(IOException::class)
	private fun readFileContent(uri: Uri): String? =
		context.contentResolver.openInputStream(uri)?.let { inputStream ->
			val reader			= BufferedReader( InputStreamReader(inputStream) )
			val stringBuilder	= StringBuilder()
			var currentline: String?

			while ((reader.readLine().also { currentline = it }) != null)
				stringBuilder.append(currentline).append("\n")

			inputStream.close()
			return stringBuilder.toString()
		}


}