package art.shittim.routing

import art.shittim.db.ArticleLine
import art.shittim.db.NoTimeArticleLine
import art.shittim.db.articleService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


fun Route.writeRoutes() {
    authenticate("write-access") {
        post("/line/append") {
            val line = call.receive<NoTimeArticleLine>()

            articleService.create(
                ArticleLine(
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    line.contrib,
                    line.line
                )
            )

            call.respond(HttpStatusCode.Created)
        }

        put("/line/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)

            articleService.update(
                id.toInt(),
                call.receive<NoTimeArticleLine>().let {
                    ArticleLine(
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        it.contrib,
                        it.line
                    )
                }
            )

            call.respond(HttpStatusCode.OK)
        }

        delete("/lines/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            articleService.delete(id.toInt())

            call.respond(HttpStatusCode.OK)
        }
    }
}