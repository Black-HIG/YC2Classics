package art.shittim

import io.ktor.client.*
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

data class TokenCheckResult(
    val valid: Boolean,
    val error: String,
    val data: Data,
    val code: Int
) {
    data class Data(
        val value: String
    )
}

val httpClient by lazy {
    HttpClient(CIO) {
        install(ContentNegotiation)
    }
}

suspend fun tokenValidity(token: String) {
    val response = httpClient.get {
        url {
            protocol = URLProtocol.HTTPS
            host = "https://uxmt.mycbxzd.top"
            path("index.php")

            parameters {
                append("api", "check")
                append("token", token)
            }
        }
    }
}


fun Application.configureSecurity() {
    install(Authentication) {
        bearer("write-access") {
            realm = "Access to modify article"

            authenticate { tokenCredential ->
                if(tokenCredential.token == "WaterStarLake") {
                    BearerTokenCredential(tokenCredential.token)
                } else {
                    null
                }
            }
        }
    }
}
