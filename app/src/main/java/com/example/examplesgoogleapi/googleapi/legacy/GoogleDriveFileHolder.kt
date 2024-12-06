package com.example.examplesgoogleapi.googleapi.legacy

import com.google.api.client.util.DateTime


class GoogleDriveFileHolder {
	var id:				String?		= null
	var name:			String?		= null
	var modifiedTime:	DateTime?	= null
	var size:			Long 		= 0
	var createdTime:	DateTime?	= null
	var starred:		Boolean?	= null
}