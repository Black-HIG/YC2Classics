package art.shittim.secure

import art.shittim.db.userService
import art.shittim.logger
import art.shittim.secure.JwtUtils.accessJwtVerifier
import art.shittim.secure.JwtUtils.MY_REALM
import art.shittim.secure.JwtUtils.makeAccessToken
import art.shittim.secure.JwtUtils.makeRefreshToken
import art.shittim.secure.JwtUtils.refreshJwtVerifier
import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun generateRandomString(length: Int): String {
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { characters.random() }
        .joinToString("")
}

val argon2: Argon2 = Argon2Factory.create()

fun argon2hash(data: String): String = argon2.hash(2, 65536, 1, data.toCharArray())

fun argon2verify(data: String, source: String) = argon2.verify(data, source.toCharArray())

fun String.hashed() = argon2hash(this)

@Serializable
data class UserPassword(
    val username: String,
    val password: String
)

@Serializable
data class TokenCredentials(
    val accessToken: String,
    val refreshToken: String,
)

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = MY_REALM
            verifier(accessJwtVerifier)

            validate { credential ->
                if(credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Access token is not valid or has expired")
            }
        }

        jwt("refresh-jwt") {
            realm = MY_REALM
            verifier(refreshJwtVerifier)

            validate { credential ->
                if(credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Refresh token is not valid or has expired")
            }
        }
    }
}

fun Route.securityRoutes() {
    post("/user/login") {
        val user = call.receive<UserPassword>()

        val perm = userService.auth(user.username, user.password)

        if(perm == -1L) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
            return@post
        }

        val accessToken = makeAccessToken(user.username, perm)
        val refreshToken = makeRefreshToken(user.username, perm)

        RefreshTokenStore[user.username] = refreshToken

        call.respond(
            TokenCredentials(
                accessToken,
                refreshToken
            )
        )

        logger.info("User {} just logged in", user.username)
    }

    authenticate("refresh-jwt") {
        post("/user/refresh") {
            val token = call.request.header(HttpHeaders.Authorization)!!.removePrefix("Bearer ")

            val username = call.principal<JWTPrincipal>()!!.payload.getClaim("username").asString()
            val perm = call.principal<JWTPrincipal>()!!.payload.getClaim("perm").asLong()

            if(RefreshTokenStore[username] == token) {
                val accessToken = makeAccessToken(username, perm)

                call.respond(
                    HttpStatusCode.OK,
                    TokenCredentials(
                        accessToken,
                        token
                    )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
            }
        }
    }

    post("/user/validate") {
        @Serializable
        data class ValidateResult(
            val valid: Boolean,
            val type: String
        )

        @Serializable
        data class ValidateRequestBody(
            val token: String
        )

        val token = call.receive<ValidateRequestBody>().token

        val isAccessToken = {
            try {
                accessJwtVerifier.verify(token)
                true
            } catch (_: Exception) {
                false
            }
        }

        val isRefreshToken = {
            try {
                val payload = refreshJwtVerifier.verify(token)!!

                val username = payload.getClaim("username").asString()

                RefreshTokenStore[username] == token
            } catch (_: Exception) {
                false
            }
        }

        call.respond(
            HttpStatusCode.OK,
            ValidateResult(
                isRefreshToken() || isAccessToken(),
                if(isRefreshToken()) "refresh" else if(isAccessToken()) "access" else "💩"
            )
        )
    }
}
