package art.shittim.routing

import art.shittim.secure.securityRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(Resources)
    install(CallLogging)
    routing {
        /*openAPI(
            path = "openapi",
            swaggerFile = "openapi/documentation.yaml",
        )*/

        route("/api") {
            readRoutes()
            writeRoutes()
            userRoutes()
            securityRoutes()
        }
    }
}
