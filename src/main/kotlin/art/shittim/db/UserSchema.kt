package art.shittim.db

import art.shittim.logger
import art.shittim.secure.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class BeanUser(
    val username: String,
    val perm: Long
)

@Serializable
data class PasswordUser(
    val username: String,
    val password: String,
    val perm: Long
)

@Serializable
data class UserData(
    val password: String,
    val perm: Long
)

class UserService(db: Database) {
    @Suppress("ExposedReference")
    object UserTable : Table("users") {
        val id = integer("id").autoIncrement()
        val username = varchar("username", 50).uniqueIndex()
        val password = varchar("password", 300)
        val perm = long("perm")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(UserTable)
        }

        runBlocking {
            val password = generateRandomString(16)

            if (readIdByName("admin") == null) {
                create(
                    PasswordUser(
                        username = "admin",
                        password = password,
                        perm = allPerm
                    )
                )

                logger.info("Auto-created admin account, password: $password")
            }
        }
    }

    suspend fun create(data: PasswordUser): Int = dbQuery {
        UserTable.insert {
            it[username] = data.username
            it[password] = data.password.hashed()
            it[perm] = data.perm
        }[UserTable.id]
    }

    @Suppress("unused")
    suspend fun read(id: Int): BeanUser? {
        return dbQuery {
            UserTable.selectAll()
                .where { UserTable.id eq id }
                .map {
                    BeanUser(
                        it[UserTable.username],
                        it[UserTable.perm]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun auth(name: String, password: String): Long {
        val row = dbQuery {
            UserTable.selectAll()
                .where { UserTable.username eq name }
                .singleOrNull()
        } ?: return -1

        return if (argon2verify(row[UserTable.password], password)) {
            row[UserTable.perm]
        } else {
            -1
        }
    }

    suspend fun readByName(name: String): BeanUser? {
        return dbQuery {
            UserTable.selectAll()
                .where { UserTable.username eq name }
                .map {
                    BeanUser(
                        it[UserTable.username],
                        it[UserTable.perm]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun readIdByName(name: String): Int? {
        return dbQuery {
            UserTable.selectAll()
                .where { UserTable.username eq name }
                .map { it[UserTable.id] }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, data: PasswordUser) {
        dbQuery {
            UserTable.update({ UserTable.id eq id }) {
                it[username] = data.username
                it[perm] = data.perm
                it[password] = data.password.hashed()
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            UserTable.deleteWhere { UserTable.id.eq(id) }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
