package model

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.varchar

object TypechoContent : Table<Nothing>("typecho_contents") {
    val cid = int("cid").primaryKey()
    val title = varchar("title")
    val slug = varchar("slug")
    val created = int("created")
    val modified = int("modified")
    val text = text("text")
    val order = int("order")
    val authorId = int("authorId")
    val template = varchar("template")
    val type = varchar("type")
    val status = varchar("status")
    val password = varchar("password")
    val commentsNum = int("commentsNum")
    val allowComment = int("allowComment")
    val allowPing = int("allowPing")
    val allowFeed = int("allowFeed")
    val parent = int("parent")
}

object TypechoComment : Table<Nothing>("typecho_comments") {
    val coid = int("coid").primaryKey()
    val cid = int("cid")
    val created = int("created")
    val author = varchar("author")
    val authorId = int("authorId")
    val ownerId = int("ownerId")
    val mail = varchar("mail")
    val url = varchar("url")
    val ip = varchar("ip")
    val agent = varchar("agent")
    val text = text("text")
    val type = varchar("type")
    val status = varchar("status")
    val parent = int("parent")
}

sealed class Meta(tableName: String) : Table<Nothing>(tableName) {
    val mid = int("mid").primaryKey()
    val name = varchar("name")
    val slug = varchar("slug")
    val type = varchar("type")
    val description = varchar("description")
    val count = int("count")
    val order = int("order")
    val parent = int("parent")
}

object TypechoCategory : Meta("typecho_metas")

object TypechoTag : Meta("typecho_metas")

object TypechoRelationship : Table<Nothing>("typecho_relationships") {
    val cid = int("cid")
    val mid = int("mid")
}