import model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import utils.ConvertUtils
import utils.HaloUtils
import utils.MarkdownUtils
import java.time.LocalDateTime


class Mover(private val typechoDB: Database, private val haloDB: Database) {

    private fun migratePostAndPage(): MutableMap<Int, Int> {
        val typechoPosts = typechoDB.from(TypechoContent).select().where { TypechoContent.type notEq "attachment" }

        // mapping old post id to new post id
        val postIdMap = mutableMapOf<Int, Int>()
        for (post in typechoPosts) {
            val postKey = haloDB.insertAndGenerateKey(Post) {
                set(it.type, if (post[TypechoContent.type]?.startsWith("post") == true) 0 else 1)
                set(it.createTime, post[TypechoContent.created]?.let { t -> ConvertUtils.timestamp2DateTime(t) })
                set(it.editTime, post[TypechoContent.created]?.let { t -> ConvertUtils.timestamp2DateTime(t) })
                set(it.updateTime, post[TypechoContent.created]?.let { t -> ConvertUtils.timestamp2DateTime(t) })
                set(it.disallowComment, if (post[TypechoContent.allowComment] == 1) 0 else 1)
                set(it.editorType, 0)
                val content = post[TypechoContent.text]?.replaceFirst("<!--markdown-->", "") ?: ""
                set(it.formatContent, MarkdownUtils.renderHtml(content))
                set(it.originalContent, content)
                set(it.slug, post[TypechoContent.slug] ?: "")
                set(
                    it.status, when (post[TypechoContent.type]) {
                        "post" -> if (post[TypechoContent.status] == "publish") 0 else 3
                        "post_draft" -> 1
                        else -> 1
                    }
                )
                set(it.title, post[TypechoContent.title] ?: "")
                set(it.topPriority, 0)
                set(it.visits, 0)
                set(it.wordCount, HaloUtils.htmlFormatWordCount(content))
            }
            println("Migrated post: ${post[TypechoContent.title]},id: ${post[TypechoContent.cid]}")
            post[TypechoContent.cid]?.let { id -> postIdMap[id] = postKey as Int }
            migrateComment(post, postKey as Int)
        }

        return postIdMap;
    }

    private fun migrateComment(post: QueryRowSet, haloPostKey: Int) {
        val queryPostComments = typechoDB.from(TypechoComment).select()
            .where { TypechoComment.cid eq (post[TypechoContent.cid]?.toInt() ?: -1) }
            .orderBy(TypechoComment.parent.asc())

        val commentIdMap = mutableMapOf<Int, Int>()
        for (comment in queryPostComments) {
            val haloCommentKey = haloDB.insertAndGenerateKey(Comment) {
                set(it.type, if (post[TypechoContent.type]?.startsWith("post") == true) 0 else 1)
                set(it.createTime, comment[TypechoComment.created]?.let { t -> ConvertUtils.timestamp2DateTime(t) })
                set(it.updateTime, comment[TypechoComment.created]?.let { t -> ConvertUtils.timestamp2DateTime(t) })
                set(it.allowNotification, 1)
                set(it.author, comment[TypechoComment.author] ?: "")
                set(it.authorUrl, comment[TypechoComment.url] ?: "")
                set(it.content, comment[TypechoComment.text] ?: "")
                set(it.email, comment[TypechoComment.mail] ?: "")
                set(it.gravatarMD5, "")
                set(it.ip, comment[TypechoComment.ip] ?: "")
                set(it.isAdmin, if (comment[TypechoComment.authorId] == 1) 1 else 0)
                set(it.postId, haloPostKey)
                set(it.status, if (comment[TypechoComment.status] == "approved") 0 else 1)
                set(it.ua, comment[TypechoComment.agent] ?: "")
                set(it.parentId, commentIdMap[comment[TypechoComment.parent]?.toInt() ?: 0] ?: 0)
            }
            comment[TypechoComment.coid]?.let { commentIdMap[it] = haloCommentKey as Int }
            println("Migrated comment: ${comment[TypechoComment.author]} : ${comment[TypechoComment.text]}")
        }
    }

    private fun migrateTag(postIdMap: MutableMap<Int, Int>) {
        val typechoTags = typechoDB.from(TypechoTag).select().where { TypechoTag.type eq "tag" }
        val now = LocalDateTime.now()
        for (tag in typechoTags) {
            val haloTagKey = haloDB.insertAndGenerateKey(Tag) {
                set(it.name, tag[TypechoTag.name] ?: "")
                set(it.createTime, now)
                set(it.updateTime, now)
                set(it.slug, tag[TypechoTag.slug] ?: "")
            }
            println("Migrated tag: ${tag[TypechoTag.name]},id: $haloTagKey")
            // migrate tag-post relation
            val tagPosts = typechoDB.from(TypechoRelationship).select()
                .where { TypechoRelationship.mid eq (tag[TypechoTag.mid]?.toInt() ?: 0) }
            for (tagPost in tagPosts) {
                tagPost[TypechoRelationship.cid]?.let {
                    haloDB.insert(PostTag) {
                        set(it.tagId, haloTagKey as Int)
                        set(it.postId, postIdMap[tagPost[TypechoRelationship.cid]?.toInt()] ?: 0)
                        set(it.createTime, now)
                        set(it.updateTime, now)
                    }
                    println("|_Migrated tag-post relation: tag: ${tag[TypechoTag.name]}, post: ${tagPost[TypechoRelationship.cid]}")
                }
            }
        }
    }

    private fun migrateCategory(postIdMap: MutableMap<Int, Int>) {
        val typechoCategories = typechoDB.from(TypechoCategory).select().where { TypechoCategory.type eq "category" }
            .orderBy(TypechoTag.parent.asc())
        val now = LocalDateTime.now()
        val categoryIdMap = mutableMapOf<Int, Int>()
        for (category in typechoCategories) {
            val haloCategoryKey = haloDB.insertAndGenerateKey(Categories) {
                set(it.name, category[TypechoCategory.name] ?: "")
                set(it.createTime, now)
                set(it.updateTime, now)
                set(it.slug, category[TypechoCategory.slug] ?: "")
                set(it.description, category[TypechoCategory.description] ?: "")
                set(it.parentId, categoryIdMap[category[TypechoCategory.parent]?.toInt() ?: 0] ?: 0)
            }
            println("Migrated category: ${category[TypechoTag.name]},id: $haloCategoryKey")
            // migrate category-post relation
            val categoryPosts = typechoDB.from(TypechoRelationship).select()
                .where { TypechoRelationship.mid eq (category[TypechoTag.mid]?.toInt() ?: 0) }
            for (categoryPost in categoryPosts) {
                haloDB.insert(PostCategory) {
                    set(it.categoryId, haloCategoryKey as Int)
                    set(it.postId, postIdMap[categoryPost[TypechoRelationship.cid]?.toInt()] ?: 0)
                    set(it.createTime, now)
                    set(it.updateTime, now)
                }
                println("|_Migrated category-post relation: category: ${category[TypechoTag.name]}, post: ${categoryPost[TypechoRelationship.cid]}")
            }
            categoryIdMap[category[TypechoTag.mid]?.toInt() ?: 0] = haloCategoryKey as Int
        }
    }

    fun start() {
        haloDB.useTransaction {
            val postIdMap = migratePostAndPage()
            migrateTag(postIdMap)
            migrateCategory(postIdMap)
        }
    }


}

