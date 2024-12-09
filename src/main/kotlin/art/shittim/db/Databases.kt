package art.shittim.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var articleService: ArticleService

@Suppress("UnusedReceiverParameter")
fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:file:./data",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    articleService = ArticleService(database)
}
