package art.shittim

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.resolve.DirectoryCodeResolver
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.jte.*
import io.ktor.server.plugins.cors.routing.*
import java.nio.file.Path

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
    }

    install(Jte) {
        val resolver = DirectoryCodeResolver(Path.of("templates"))
        templateEngine = TemplateEngine.create(resolver, ContentType.Html)
    }
}

