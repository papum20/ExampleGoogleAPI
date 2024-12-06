@file:Suppress("FunctionName")

package com.example.examplesgoogleapi.utils

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload


class ExtensionsGoogle {

	companion object {

		fun Payload.getName():			String? = get("name")?.toString()
		fun Payload.getGivenName():		String? = get("given_name")?.toString()
		fun Payload.getFamilyName():	String? = get("family_name")?.toString()
		fun Payload.getLocale():		String? = get("locale")?.toString()
		fun Payload.getPictureUrl():	String? = get("picture")?.toString()

	}
}