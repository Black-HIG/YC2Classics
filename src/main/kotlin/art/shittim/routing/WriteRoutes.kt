package art.shittim.routing

import art.shittim.db.ArticleService
import art.shittim.logger
import art.shittim.secure.PArticleModify
import art.shittim.secure.authenticatePerm
import art.shittim.utils.UUID
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@Serializable
data class RequestArticleLine(
    var time: String?,
    val contrib: String,
    val line: String,
    val unsure: Boolean,
    val sensitive: Boolean,
)

fun Route.writeRoutes() {
    authenticate("auth-jwt") {
        authenticatePerm(PArticleModify) {
            post("/line/append") {
                val request = call.receive<RequestArticleLine>()

                /*articleService.create(
                    ArticleLine(
                        line.time ?: "不详",
                        line.contrib,
                        line.line
                    )
                )*/

                newSuspendedTransaction {
                    ArticleService.ArticleEntity.new {
                        time = request.time ?: "不详"
                        contrib = request.contrib
                        line = request.line
                        unsure = request.unsure
                        sensitive = request.sensitive
                    }
                }

                call.respond(HttpStatusCode.Created)
                logger.info("Created ${if(request.unsure) "unsure" else ""} ${if(request.sensitive) "sensitive" else ""} line {}:{}", request.contrib, request.line)
            }

            put("/line/{id}") {
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<RequestArticleLine>()

                newSuspendedTransaction {
                    ArticleService.ArticleEntity.findByIdAndUpdate(UUID.fromString(id)) {
                        it.time = request.time ?: "不详"
                        it.contrib = request.contrib
                        it.line = request.line
                        it.unsure = request.unsure
                        it.sensitive = request.sensitive
                    }
                }

                call.respond(HttpStatusCode.OK)
                logger.info("Updated ${if(request.unsure) "unsure" else ""} ${if(request.sensitive) "sensitive" else ""} line {}:{}", request.contrib, request.line)
            }

            delete("/line/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val line = newSuspendedTransaction { ArticleService.ArticleEntity.findById(UUID.fromString(id)) }
                    ?: return@delete call.respond(HttpStatusCode.NotFound)
                newSuspendedTransaction { line.delete() }

                call.respond(HttpStatusCode.OK)
                logger.info("Deleted ${if(line.unsure) "unsure" else ""} ${if(line.sensitive) "sensitive" else ""} line {}:{}", line.contrib, line.line)
            }
        }
    }
}