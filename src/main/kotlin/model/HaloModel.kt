package model

import org.ktorm.schema.*

object Post : Table<Nothing>("posts") {
    /**
     * 0 post, 1 page
     */
    val type = int("type")

    val id = int("id").primaryKey()

    val createTime = datetime("create_time")

    val updateTime = datetime("update_time")

    /**
     * Ktorm not support bit type, default 0
     */
    val disallowComment = int("disallow_comment")

    val editTime = datetime("edit_time")

    /**
     * 0 markdown, 1 rich text
     */
    val editorType = int("editor_type")

    /**
     * Rendered content.
     */
    val formatContent = text("format_content")

    /**
     * no migrate
     */
    val likes = int("likes")

    val metaDescription = varchar("meta_description")

    val metaKeywords = varchar("meta_keywords")

    /**
     * Original content,not format
     */
    val originalContent = text("original_content")

    val password = varchar("password")

    /**
     * no migrate
     */
    val slug = varchar("slug")

    /**
     * 0 published, 1 draft, 2 recycle, 3 intimate
     */
    val status = int("status")

    /**
     *  no migrate
     */
    val summary = text("summary")

    /**
     *  no migrate
     */
    val template = varchar("template")

    /**
     *  no migrate
     */
    val thumbnail = varchar("thumbnail")

    val title = varchar("title")

    val topPriority = int("top_priority")

    /**
     * deprecated in halo
     */
    val url = varchar("url")

    /**
     * no migrate
     */
    val visits = int("visits")

    /**
     * counting using utils.HaloUtils.Companion#htmlFormatWordCount
     */
    val wordCount = long("word_count")
}

object Comment : Table<Nothing>("comments") {
    val id = int("id").primaryKey()
    val type = int("type")
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time")
    val allowNotification = int("allow_notification")
    val author = varchar("author")
    val authorUrl = varchar("author_url")
    val content = varchar("content")
    val email = varchar("email")
    val gravatarMD5 = varchar("gravatar_md5")
    val ip = varchar("ip_address")
    val isAdmin = int("is_admin")
    val parentId = int("parent_id")
    val postId = int("post_id")
    // 0 published, 1 auditing, 2 recycle
    val status = int("status")
    val topPriority = int("top_priority")
    val ua = varchar("user_agent")
}

object Tag : Table<Nothing>("tags") {
    val id = int("id").primaryKey()
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time")
    val name = varchar("name")
    val slug = varchar("slug")
    val slugName = varchar("slug_name")
    val thumbnail = varchar("thumbnail")
}

object Categories : Table<Nothing>("categories") {
    val id = int("id").primaryKey()
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time")
    val description = varchar("description")
    val name = varchar("name")
    val parentId = int("parent_id")
    val password = varchar("password")
    val slug = varchar("slug")
    val slugName = varchar("slug_name")
    val thumbnail = varchar("thumbnail")
}

object PostTag : Table<Nothing>("post_tags") {
    val id = int("id").primaryKey()
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time")
    val postId = int("post_id")
    val tagId = int("tag_id")
}

object PostCategory : Table<Nothing>("post_categories") {
    val id = int("id").primaryKey()
    val createTime = datetime("create_time")
    val updateTime = datetime("update_time")
    val categoryId = int("category_id")
    val postId = int("post_id")
}