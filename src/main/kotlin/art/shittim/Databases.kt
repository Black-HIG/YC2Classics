package art.shittim

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*

lateinit var articleService: ArticleService

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:file:./data",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    articleService = ArticleService(database)
}
