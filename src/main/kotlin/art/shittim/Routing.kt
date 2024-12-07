package art.shittim

import art.shittim.routing.readRoutes
import art.shittim.routing.writeRoutes
import io.ktor.server.application.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(Resources)
    routing {
        readRoutes()
        writeRoutes()
    }
}
