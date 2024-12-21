package art.shittim.db

import art.shittim.logger
import art.shittim.secure.allPerm
import art.shittim.secure.argon2verify
import art.shittim.secure.generateRandomString
import art.shittim.secure.hashed
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

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
    object UserTable : UUIDTable("users") {
        val username = varchar("username", 50).uniqueIndex()
        val password = varchar("password", 300)
        val perm = long("perm")
    }

    class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<UserEntity>(UserTable) {
            fun findByName(username: String) =
                find { UserTable.username eq username }

            fun findByNameAndUpdate(username: String, block: (it: UserEntity) -> Unit) =
                findSingleByAndUpdate(UserTable.username eq username, block)
        }

        var username by UserTable.username
        var password by UserTable.password
        var perm by UserTable.perm
    }

    init {
        transaction(db) {
            SchemaUtils.create(UserTable)
        }

        runBlocking {
            val generatedPassword = generateRandomString(16)

            newSuspendedTransaction {
                val admin = UserEntity.find { UserTable.username eq "admin" }.singleOrNull()?: run {
                    UserEntity.new {
                        username = "admin"
                        password = generatedPassword.hashed()
                        perm = allPerm
                    }

                    logger.info("Auto-created admin account, password: $generatedPassword")

                    return@newSuspendedTransaction
                }

                if(admin.perm != allPerm) {
                    admin.perm = allPerm
                    logger.info("Privileged admin account")
                }
            }
            /*if (readIdByName("admin") == null) {
                create(
                    PasswordUser(
                        username = "admin",
                        password = password,
                        perm = allPerm
                    )
                )

                logger.info("Auto-created admin account, password: $password")
            }*/
        }
    }

    suspend fun auth(name: String, password: String): Long {
        return newSuspendedTransaction {
            val user = UserEntity.find {UserTable.username eq name }.singleOrNull() ?: return@newSuspendedTransaction -1

            if (argon2verify(user.password, password)) {
                user.perm
            } else {
                -1
            }
        }
    }
}
