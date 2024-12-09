package art.shittim.db

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ArticleLine(val line: String)

class ArticleService(db: Database) {
    object ArticleTable : Table() {
        val id = integer("id").autoIncrement()
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
        }[ArticleTable.id]
    }

    suspend fun read(id: Int): ArticleLine? {
        return dbQuery {
            ArticleTable.selectAll()
                .where { ArticleTable.id eq id }
                .map { ArticleLine(it[ArticleTable.line]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, data: ArticleLine) {
        dbQuery {
            ArticleTable.update({ ArticleTable.id eq id }) {
                it[line] = data.line
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
