package com.example.examplesgoogleapi.utils.structures

import com.example.examplesgoogleapi.utils.ExtensionsGoogle.Companion.getFamilyName
import com.example.examplesgoogleapi.utils.ExtensionsGoogle.Companion.getGivenName
import com.example.examplesgoogleapi.utils.ExtensionsGoogle.Companion.getLocale
import com.example.examplesgoogleapi.utils.ExtensionsGoogle.Companion.getName
import com.example.examplesgoogleapi.utils.ExtensionsGoogle.Companion.getPictureUrl
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload


/**
 * @param subject user identifier, which can be used for authentication and authorization,
 * being secure, and for storing user data, being stable
 */
data class GoogleTokenPayload(
	val subject:		String?,
	val email:			String?,
	val emailVerified:	Boolean?,
	val name:			String?,
	val familyName:		String?,
	val givenName:		String?,
	val locale:			String?,
	val pictureUrl:		String?,
) {

	override fun toString(): String =
		"$subject, $email, $emailVerified, $name, $familyName, $givenName, $locale, $pictureUrl"



	companion object {

		fun fromPayload(payload: Payload) = GoogleTokenPayload(
			subject			= payload.subject,
			email			= payload.email,
			emailVerified	= payload.emailVerified,
			familyName		= payload.getFamilyName(),
			givenName		= payload.getGivenName(),
			locale			= payload.getLocale(),
			name			= payload.getName(),
			pictureUrl		= payload.getPictureUrl()
		)

	}

}