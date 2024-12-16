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
        "jdbc:postgresql://db:5432/classics_database"
    }
    val database = Database.connect(
        url = jdbcUrl,
        user = "postgres",
        driver = "org.postgresql.Driver",
        password = "password",
    )
    //val database = config.database.database
    articleService = ArticleService(database)
    userService = UserService(database)
}
