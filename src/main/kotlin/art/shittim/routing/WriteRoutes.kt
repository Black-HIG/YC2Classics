package art.shittim.routing

import art.shittim.db.ArticleLine
import art.shittim.db.articleService
import art.shittim.logger
import art.shittim.secure.PArticleModify
import art.shittim.secure.authenticatePerm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class RequestArticleLine(
    var time: String?,
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
                        line.time ?: "不详",
                        line.contrib,
                        line.line
                    )
                )

                call.respond(HttpStatusCode.Created)
                logger.info("Created new line {}:{}", line.contrib, line.line)
            }

            put("/line/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val line = call.receive<RequestArticleLine>().let {
                    ArticleLine(
                        it.time ?: "不详",
                        it.contrib,
                        it.line
                    )
                }

                articleService.update(
                    id.toInt(),
                    line
                )

                call.respond(HttpStatusCode.OK)
                logger.info("Updated line {}, new data: {}:{}", id, line.contrib, line.line)
            }

            delete("/line/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val line = articleService.read(id.toInt()) ?: return@delete call.respond(HttpStatusCode.NotFound)
                articleService.delete(id.toInt())

                call.respond(HttpStatusCode.OK)
                logger.info("Removed line {}, original data: {}:{}", id, line.contrib, line.line)
            }
        }
    }
}