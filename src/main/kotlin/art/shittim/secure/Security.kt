package art.shittim.secure

import art.shittim.config
import art.shittim.db.userService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class TokenCheckResult(
    val valid: Boolean,
    val error: String?,
    val data: Data?,
    val code: Int
) {
    @Serializable
    data class Data(
        val value: Int
    )
}

val httpClient by lazy {
    HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
}

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

@Deprecated(message = "Use jwt rather than token api", level = DeprecationLevel.HIDDEN)
suspend fun tokenValidity(token: String): Boolean {
    val response = httpClient.get {
        url {
            protocol = URLProtocol.HTTPS
            //host = config.token.url

            //path(config.token.path)

            parameters.append("api", "check")
            parameters.append("token", token)
        }
    }

    val result : TokenCheckResult = response.body()

    return if(result.valid) {
        result.data!!.value and 0b0100 > 0
    } else {
        false
    }
}

const val secret = "yc2_waterstarlake"
const val issuer = "classics.shittim.art"
const val audience = "classics.shittim.art"
const val myRealm = "YC2 Classics Access"

@Serializable
data class UserPassword(
    val username: String,
    val password: String
)

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
            )

            validate { credential ->
                if(credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }


    }

    routing {
        post("/login") {
            val user = call.receive<UserPassword>()

            val perm = userService.auth(user.username, user.password)

            if(perm == -1) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
                return@post
            }

            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", user.username)
                .withClaim("perm", perm)
                .withExpiresAt(Date(System.currentTimeMillis() + 600 * 1000))
                .sign(Algorithm.HMAC256(secret))

            call.respond(hashMapOf("token" to token))
        }
    }
}
