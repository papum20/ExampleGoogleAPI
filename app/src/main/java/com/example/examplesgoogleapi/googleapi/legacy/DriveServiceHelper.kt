package com.example.examplesgoogleapi.googleapi.legacy

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class DriveServiceHelper(private val mDriveService: Drive) {

	private val mExecutor: Executor = Executors.newSingleThreadExecutor()
	private val TAG = "DRIVE_TAG"


	/**
	 * Creates a text file in the user's My Drive folder and returns its file ID.
	 */
	fun createFile(filename: String = "Untitled file", folderId: String = "root"): Task<GoogleDriveFileHolder> {
		return Tasks.call(mExecutor) {
			GoogleDriveFileHolder().apply {
				val metadata = File()
					.setParents(listOf(folderId))
					.setMimeType("text/plain")
					.setName(filename)

				val googleFile = mDriveService.files().create(metadata).execute()
					?: throw IOException("Null result when requesting file creation.")
				id = googleFile.id
			}
		}
	}


	// TO CREATE A FOLDER
	fun createFolder(
		folderName: String,
		folderId: String = "root"
	): Task<GoogleDriveFileHolder> {
		return Tasks.call(mExecutor) {
			GoogleDriveFileHolder().apply {

				val metadata = File()
					.setParents(listOf(folderId))
					.setMimeType("application/vnd.google-apps.folder")
					.setName(folderName)

				val googleFile = mDriveService.files().create(metadata).execute()
					?: throw IOException("Null result when requesting file creation.")
				id = googleFile.id
			}
		}
	}


	fun downloadFile(targetFile: java.io.File?, fileId: String?): Task<Void?> {
		return Tasks.call(
			mExecutor
		) {
			// Retrieve the metadata as a File object.
			val outputStream: OutputStream = FileOutputStream(targetFile)
			mDriveService.files()[fileId].executeMediaAndDownloadTo(outputStream)
			null
		}
	}

	fun deleteFolderFile(fileId: String?): Task<Void?> {
		return Tasks.call(
			mExecutor
		) {
			// Retrieve the metadata as a File object.
			if (fileId != null) {
				mDriveService.files().delete(fileId).execute()
			}
			null
		}
	}

	// TO LIST FILES
	@Throws(IOException::class)
	fun listDriveImageFiles(): List<File> {
		var result: FileList
		var pageToken: String? = null
		do {
			result = mDriveService.files()
				.list() /*.setQ("mimeType='image/png' or mimeType='text/plain'")This si to list both image and text files. Mind the type of image(png or jpeg).setQ("mimeType='image/png' or mimeType='text/plain'") */
				.setSpaces("drive")
				.setFields("nextPageToken, files(id, name)")
				.setPageToken(pageToken)
				.execute()

			pageToken = result.nextPageToken
		} while (pageToken != null)

		return result.files
	}

	// TO UPLOAD A FILE ONTO DRIVE
	fun uploadFile(
		localFile: java.io.File,
		mimeType: String?, folderId: String?
	): Task<GoogleDriveFileHolder?> {
		return Tasks.call(mExecutor) { // Retrieve the metadata as a File object.
			val root: List<String> = if (folderId == null) {
				listOf("root")
			} else {
				listOf(folderId)
			}

			val metadata = File()
				.setParents(root)
				.setMimeType(mimeType)
				.setName(localFile.name)

			val fileContent = FileContent(mimeType, localFile)

			val fileMeta = mDriveService.files().create(
				metadata,
				fileContent
			).execute()
			val googleDriveFileHolder = GoogleDriveFileHolder()
			googleDriveFileHolder.id = fileMeta.id
			googleDriveFileHolder.name = fileMeta.name
			googleDriveFileHolder
		}
	}
}