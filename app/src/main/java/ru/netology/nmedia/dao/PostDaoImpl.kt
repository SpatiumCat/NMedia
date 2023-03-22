package ru.netology.nmedia.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.Post

class PostDaoImpl(private val db: SQLiteDatabase) : PostDao {

    companion object {
        val DDL_Post = """
            CREATE TABLE ${PostColumns.TABLE} (
            ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
            ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
            ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL,
            ${PostColumns.COLUMN_VIDEO} TEXT NOT NULL,
            ${PostColumns.COLUMN_LIKED_BY_ME} BOOLEAN NOT NULL DEFAULT 0,
            ${PostColumns.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 1099,
            ${PostColumns.COLUMN_SHARES} INTEGER NOT NULL DEFAULT 1000
            );
        """.trimIndent()

        val DDL_Draft = """
            CREATE TABLE ${PostDraftColumns.TABLE} (
            ${PostDraftColumns.COLUMN_CONTENT} TEXT NOT NULL DEFAULT (empty)
            );
        """.trimIndent()
    }

    object PostColumns {
        const val TABLE = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_VIDEO = "video"
        const val COLUMN_LIKED_BY_ME = "likedByMe"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_SHARES = "shares"
        val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_AUTHOR,
            COLUMN_CONTENT,
            COLUMN_PUBLISHED,
            COLUMN_VIDEO,
            COLUMN_LIKED_BY_ME,
            COLUMN_LIKES,
            COLUMN_SHARES
        )
    }


    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()

        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            if (post.id != 0L) {
                put(PostColumns.COLUMN_ID, post.id)
            }
            put(PostColumns.COLUMN_AUTHOR, "Me")
            put(PostColumns.COLUMN_CONTENT, post.content)
            put(PostColumns.COLUMN_PUBLISHED, "now")
            put(PostColumns.COLUMN_VIDEO, post.video)
        }
        val id = db.replace(PostColumns.TABLE, null, values)

        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likeById(id: Long) {
        db.execSQL(
            """
                UPDATE posts SET
                likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
                likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END 
                WHERE id = ?;
            """.trimIndent(), arrayOf(id)
        )
    }

    override fun removeById(id: Long) {
        db.delete(
            PostColumns.TABLE,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )

    }

    override fun shareById(id: Long) {
        db.execSQL(
            """
                UPDATE posts SET
                shares = shares + 10 WHERE id = ?;
            """.trimIndent(), arrayOf(id)
        )
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                id = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                video = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEO)),
                likedByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKED_BY_ME)) != 0,
                likes = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                shares = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARES))
            )
        }
    }

    object PostDraftColumns {
        const val TABLE = "draft"
        const val COLUMN_CONTENT = "content"
        val ALL_COLUMNS = arrayOf(
            COLUMN_CONTENT
        )
    }

    override fun getDraft(): String? {
        db.query(
            PostDraftColumns.TABLE,
            PostDraftColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            null
        ).use {
            return if (it.moveToNext()) {
                mapDraft(it)
            } else {
                null
            }
        }
    }

    override fun saveDraft(content: String): String {
        val values = ContentValues().apply {
            put(PostDraftColumns.COLUMN_CONTENT, content)
        }
        db.delete(PostDraftColumns.TABLE, null, null)
        db.replace(PostDraftColumns.TABLE, null, values)
        db.query(
            PostDraftColumns.TABLE,
            PostDraftColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            null
        ).use {
            it.moveToNext()
            return mapDraft(it)
        }
    }

    private fun mapDraft(cursor: Cursor): String {
        with(cursor) {
            return getString(getColumnIndexOrThrow(PostDraftColumns.COLUMN_CONTENT))
        }
    }
}
