package com.samarth.authentication

import io.ktor.auth.*
import io.ktor.http.*

val googleOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "google",
    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
    requestMethod = HttpMethod.Post,
    clientId = "595063996477-le0agvbqrlk4vllj4349jt5bhsqlqvr8.apps.googleusercontent.com",
    clientSecret = "mB6E-U70pzn04EXiVnt5sQ3d",
    defaultScopes = listOf("profile")
)