package art.shittim.routing

import art.shittim.db.ArticleService
import art.shittim.routing.Read.JsonWithTally
import art.shittim.secure.PAccountList
import art.shittim.secure.hasPerm
import art.shittim.utils.UUID
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@Suppress("unused")
@Resource("/read")
class Read {
    @Resource("/json_with_tally") // J W T
    class JsonWithTally(val parent: Read = Read())

    @Resource("json")
    class Json(val parent: Read = Read())
}

@Serializable
data class IdArticleLine(
    val id: UUID,
    val line: String,
    val time: String,
    val contrib: String,
    val unsure: Boolean,
    val sensitive: Boolean,
    val hidden: Boolean,
)

@Serializable
data class JsonWithTallyResponse(
    val lines: List<IdArticleLine>,
    val tally: Int
)

const val kivo = "https://static.kivo.wiki/images"

@Suppress("NonAsciiCharacters", "ObjectPropertyName")
val `ウシオ-ノアs` = listOf(
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_8_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_4_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_5_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_6_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_1_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_7_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_3_waifu2x_2x_1n_png.jpg",
    "/kivo/students/生盐 诺亚/gallery/初始立绘差分/J_2_waifu2x_2x_1n_png.jpg"
)

fun Route.readRoutes() {
    authenticate("auth-jwt", optional = true) {
        get<Read.Json> {
            hasPerm(
                listOf(PAccountList),
                {
                    val lines = newSuspendedTransaction {
                        ArticleService.ArticleEntity.all().map {
                            IdArticleLine(
                                it.id.value,
                                it.line,
                                it.time,
                                it.contrib,
                                it.unsure,
                                it.sensitive,
                                it.hidden
                            )
                        }
                    }

                    call.respond(HttpStatusCode.OK, lines)
                },
                {
                    val lines = newSuspendedTransaction {
                        ArticleService.ArticleEntity.all()
                            .filter { it.hidden == false }
                            .map {
                            IdArticleLine(
                                it.id.value,
                                it.line,
                                it.time,
                                it.contrib,
                                it.unsure,
                                it.sensitive,
                                it.hidden
                            )
                        }
                    }

                    call.respond(HttpStatusCode.OK, lines)
                }
            )
        }

        get<JsonWithTally> {
            hasPerm(
                listOf(PAccountList),
                {
                    val lines = newSuspendedTransaction {
                        ArticleService.ArticleEntity.all().map {
                            IdArticleLine(
                                it.id.value,
                                it.line,
                                it.time,
                                it.contrib,
                                it.unsure,
                                it.sensitive,
                                it.hidden
                            )
                        }
                    }

                    call.respond(HttpStatusCode.OK, JsonWithTallyResponse(lines, lines.size))
                },
                {
                    val lines = newSuspendedTransaction {
                        ArticleService.ArticleEntity.all()
                            .filter { it.hidden == false }
                            .map {
                                IdArticleLine(
                                    it.id.value,
                                    it.line,
                                    it.time,
                                    it.contrib,
                                    it.unsure,
                                    it.sensitive,
                                    it.hidden
                                )
                            }
                    }

                    call.respond(HttpStatusCode.OK, JsonWithTallyResponse(lines, lines.size))
                }
            )
        }
    }

    get("/") {
        call.respondRedirect("/read/html")
    }

    get("/line/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val line = try {
            newSuspendedTransaction {
                ArticleService.ArticleEntity.findById(UUID.fromString(id))
            } ?: return@get call.respond(HttpStatusCode.NotFound)
            //articleService.read(id.toInt()) ?: return@get call.respond(HttpStatusCode.NotFound)
        } catch (_: Exception) {
            return@get call.respond(HttpStatusCode.NotFound)
        }
        call.respond(
            HttpStatusCode.OK,
            IdArticleLine(
                line.id.value,
                line.line,
                line.time,
                line.contrib,
                line.unsure,
                line.sensitive,
                line.hidden
            )
        )
    }
}
