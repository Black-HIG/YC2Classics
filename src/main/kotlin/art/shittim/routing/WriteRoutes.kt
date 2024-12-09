package art.shittim.routing

import art.shittim.db.ArticleLine
import art.shittim.db.articleService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.writeRoutes() {
    authenticate("write-access") {
        post("/line/append") {
            val line = call.receive<ArticleLine>()

            articleService.create(ArticleLine(line.line))

            call.respond(HttpStatusCode.Created)
        }

        put("/line/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)

            articleService.update(id.toInt(), call.receive<ArticleLine>())

            call.respond(HttpStatusCode.OK)
        }

        delete("/lines/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            articleService.delete(id.toInt())

            call.respond(HttpStatusCode.OK)
        }
    }
}