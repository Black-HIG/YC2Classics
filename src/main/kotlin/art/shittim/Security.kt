package art.shittim

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

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

suspend fun tokenValidity(token: String): Boolean {
    val response = httpClient.get {
        url {
            protocol = URLProtocol.HTTPS
            host = "uxmt.mycbxzd.top"
            path("index.php")

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


fun Application.configureSecurity() {
    install(Authentication) {
        bearer("write-access") {
            realm = "Access to modify article"

            authenticate { tokenCredential ->
                /*if(tokenCredential.token == "WaterStarLake") {
                    BearerTokenCredential(tokenCredential.token)
                } else {
                    null
                }*/
                if(tokenValidity(tokenCredential.token)) {
                    BearerTokenCredential(tokenCredential.token)
                } else {
                    null
                }
            }
        }
    }
}
