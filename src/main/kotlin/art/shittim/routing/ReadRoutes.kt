package art.shittim.routing

import art.shittim.db.ArticleService
import art.shittim.db.articleService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll
import java.time.format.DateTimeFormatter

@Suppress("unused")
@Resource("/read")
class Read {
    @Resource("json")
    class Json(val parent: Read = Read())

    @Resource("plain")
    class Plain(val parent: Read = Read())

    @Resource("markdown")
    class Markdown(val parent: Read = Read())

    @Resource("html")
    class Html(val parent: Read = Read())
}

@Serializable
data class IdArticleLine(
    val id: Int,
    val line: String,
    val time: LocalDateTime,
    val contrib: String
)

@Serializable
data class PebArticleLine(
    val line: String,
    val time: String,
    val contrib: String
)

fun Route.readRoutes() {
    get<Read.Json> {
        application.log.info("Reading json")
        val lines = articleService.dbQuery {
            ArticleService.ArticleTable.selectAll()
                .map {
                    IdArticleLine(
                        it[ArticleService.ArticleTable.id],
                        it[ArticleService.ArticleTable.line],
                        it[ArticleService.ArticleTable.time],
                        it[ArticleService.ArticleTable.contrib]
                    )
                }
                .toList()
        }

        call.respond(lines)
    }

    get<Read.Plain> {
        val lines = articleService.dbQuery {
            ArticleService.ArticleTable.selectAll()
                .map {
                    it[ArticleService.ArticleTable.line]
                }
                .toList()
        }

        call.respond(lines.joinToString(separator = "\n\n"))
    }

    get<Read.Markdown> {
        val sb = StringBuilder()
        var i = 0

        sb.appendLine("# 英才二班典籍\n")

        articleService.dbQuery {
            ArticleService.ArticleTable.selectAll()
                .map {
                    it[ArticleService.ArticleTable.line]
                }
                .forEach {
                    sb.appendLine("${++i}.  $it")
                }
        }

        call.respond(sb.toString())
    }

    get<Read.Html> {
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")

        val lines = articleService.dbQuery {
            ArticleService.ArticleTable.selectAll()
                .map {
                    IdArticleLine(
                        it[ArticleService.ArticleTable.id],
                        it[ArticleService.ArticleTable.line],
                        it[ArticleService.ArticleTable.time],
                        it[ArticleService.ArticleTable.contrib]
                    )
                }
                .toList()
        }.map {
            PebArticleLine(
                it.line,
                it.time.toJavaLocalDateTime().format(formatter),
                it.contrib
            )
        }
        
        call.respond(PebbleContent("article.peb", mapOf("lines" to lines)))
    }

    get("/line/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val line = try {
            articleService.read(id.toInt()) ?: return@get call.respond(HttpStatusCode.NotFound)
        } catch (e: Exception) {
            return@get call.respond(HttpStatusCode.NotFound)
        }
        call.respond(line)
    }
}
