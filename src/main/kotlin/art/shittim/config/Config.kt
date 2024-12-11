package art.shittim.config

import org.jetbrains.exposed.sql.Database

enum class DatabaseType {
    MySQL {
        override fun build(
            host: String,
            port: Int,
            name: String
        ) = "jdbc:mysql://$host:$port/$name"

        override fun driver() = "com.mysql.cj.jdbc.Driver"
    },
    PostgreSQL {
        override fun build(
            host: String,
            port: Int,
            name: String
        ): String = "jdbc:postgresql://$host:$port/$name"

        override fun driver() = "org.postgresql.Driver"
    },
    SQLite {
        override fun build(
            host: String,
            port: Int,
            name: String
        ) = "jdbc:sqlite:./data.db"

        override fun driver() = "org.sqlite.JDBC"
    },
    H2 {
        override fun build(
            host: String,
            port: Int,
            name: String
        ) = "jdbc:h2:file:./data"

        override fun driver() = "org.h2.Driver"
    };

    open fun build(
        host: String,
        port: Int,
        name: String
    ) = ""

    open fun driver() = ""
}

data class CDatabase(
    val type: DatabaseType,
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String
) {
    val database by lazy {
        Database.connect(
            url = type.build(host, port, name),
            user = user,
            password = password,
            driver = type.driver()
        )
    }
}

data class CServer(
    val port: Int
)

data class CWeb(
    val footer: String,
    val header: String
)

data class CConfig(
    val database: CDatabase,
    val server: CServer,
    val web: CWeb
)