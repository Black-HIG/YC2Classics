package art.shittim

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.plugins.cors.routing.*
import io.pebbletemplates.pebble.loader.ClasspathLoader

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
    }

    install(Pebble) {
        loader(ClasspathLoader())
    }
}

