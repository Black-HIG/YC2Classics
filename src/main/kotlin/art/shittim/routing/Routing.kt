package art.shittim.routing

import art.shittim.secure.securityRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(Resources)
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
