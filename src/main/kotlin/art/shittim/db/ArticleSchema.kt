package art.shittim.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ArticleService(db: Database) {
    @Suppress("ExposedReference")
    object ArticleTable : UUIDTable("articles") {
        val time = text("time")
        val contrib = text("contributor")
        val line = text("line")
        val unsure = bool("unsure")
        val sensitive = bool("sensitive")
        val hidden = bool("hidden").nullable()
    }

    class ArticleEntity(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<ArticleEntity>(ArticleTable)

        var time by ArticleTable.time
        var contrib by ArticleTable.contrib
        var line by ArticleTable.line
        var unsure by ArticleTable.unsure
        var sensitive by ArticleTable.sensitive
        var hidden by ArticleTable.hidden
    }

    init {
        transaction(db) {
            SchemaUtils.create(ArticleTable)
        }
    }
}
