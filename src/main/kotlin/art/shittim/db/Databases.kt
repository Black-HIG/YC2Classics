package art.shittim.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var articleService: ArticleService
lateinit var userService: UserService

@Suppress("UnusedReceiverParameter")
fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:file:./data",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    //val database = config.database.database
    articleService = ArticleService(database)
    userService = UserService(database)
}
