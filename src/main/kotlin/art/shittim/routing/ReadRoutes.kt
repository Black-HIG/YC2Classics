package art.shittim.routing

import art.shittim.ArticleLine
import art.shittim.ArticleService
import art.shittim.articleService
import io.ktor.resources.*
import io.ktor.server.jte.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.selectAll

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
data class IdArticleLine(val id: Int, val line: String)

fun Route.readRoutes() {
    get<Read.Json> {
        val lines = articleService.dbQuery {
            ArticleService.ArticleTable.selectAll()
                .map {
                    IdArticleLine(
                        it[ArticleService.ArticleTable.id],
                        it[ArticleService.ArticleTable.line]
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
        val lines = articleService.dbQuery {
            ArticleService.ArticleTable.selectAll()
                .map {
                    IdArticleLine(
                        it[ArticleService.ArticleTable.id],
                        it[ArticleService.ArticleTable.line]
                    )
                }
                .toList()
        }

        call.respond(JteContent("article.kte", mapOf("lines" to lines)))
    }
}


