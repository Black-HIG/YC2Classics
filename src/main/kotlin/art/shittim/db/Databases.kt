package art.shittim.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var articleService: ArticleService
lateinit var userService: UserService

@Suppress("UnusedReceiverParameter")
fun Application.configureDatabases() {
    val jdbcUrl = if (System.getenv("DEV") == "1") {
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    } else {
        "jdbc:h2:/var/lib/classics/db"
    }
    val database = Database.connect(
        url = jdbcUrl,
        user = "postgres", // NTR
        password = "password",
    )
    //val database = config.database.database
    articleService = ArticleService(database)
    userService = UserService(database)
}
