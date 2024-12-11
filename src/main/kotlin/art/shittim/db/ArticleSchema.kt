package art.shittim.db

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ArticleLine(
    val time: LocalDateTime,
    val contrib: String,
    val line: String
)

class ArticleService(db: Database) {
    @Suppress("ExposedReference")
    object ArticleTable : Table("articles") {
        val id = integer("id").autoIncrement()
        val time = datetime("time")
        val contrib = text("contributor")
        val line = text("line")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(db) {
            SchemaUtils.create(ArticleTable)
        }
    }

    suspend fun create(data: ArticleLine): Int = dbQuery {
        ArticleTable.insert {
            it[line] = data.line
            it[time] = data.time
            it[contrib] = data.contrib
        }[ArticleTable.id]
    }

    suspend fun read(id: Int): ArticleLine? {
        return dbQuery {
            ArticleTable.selectAll()
                .where { ArticleTable.id eq id }
                .map { ArticleLine(
                    it[ArticleTable.time],
                    it[ArticleTable.contrib],
                    it[ArticleTable.line]
                ) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, data: ArticleLine) {
        dbQuery {
            ArticleTable.update({ ArticleTable.id eq id }) {
                it[line] = data.line
                it[time] = data.time
                it[contrib] = data.contrib
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            ArticleTable.deleteWhere { ArticleTable.id.eq(id) }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
