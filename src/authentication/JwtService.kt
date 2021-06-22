package com.samarth.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.samarth.models.User

class JwtService {

    private val issuer = "memesServer"
    private val jwtSecret = System.getenv("JWT_SECRET")
    private val algorithm = Algorithm.HMAC512(jwtSecret)

    val verifier:JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(user: User):String {
        return JWT.create()
            .withSubject("NOteAuthentication")
            .withIssuer(issuer)
            .withClaim("email",user.userInfo.email)
            .sign(algorithm)
    }

}