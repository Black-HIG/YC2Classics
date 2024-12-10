package art.shittim.routing

import art.shittim.db.ArticleLine
import art.shittim.db.articleService
import art.shittim.secure.PArticleModify
import art.shittim.secure.authenticatePerm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class RequestArticleLine(
    var time: LocalDateTime?,
    val contrib: String,
    val line: String
)

fun Route.writeRoutes() {
    authenticate("auth-jwt") {
        authenticatePerm(PArticleModify) {
            post("/line/append") {
                val line = call.receive<RequestArticleLine>()

                articleService.create(
                    ArticleLine(
                        line.time ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
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
                    call.receive<RequestArticleLine>().let {
                        ArticleLine(
                            it.time ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                            it.contrib,
                            it.line
                        )
                    }
                )

                call.respond(HttpStatusCode.OK)
            }

            delete("/line/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                articleService.delete(id.toInt())

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}