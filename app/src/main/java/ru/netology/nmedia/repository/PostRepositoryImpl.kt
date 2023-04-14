package ru.netology.nmedia.repository


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.Post
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.entity.DraftEntity
import java.util.concurrent.TimeUnit


class PostRepositoryImpl(private val draftDao: DraftDao) : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://192.168.0.212:9999"
        private val jsonType = "application/json".toMediaType()
    }


    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts").build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let { gson.fromJson(it, typeToken.type) }
    }


    override fun likeById(id: Long): Post {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id/likes")
            .post(gson.toJson(id).toRequestBody(jsonType))
            .build()

            return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let { gson.fromJson(it, Post::class.java) }
    }

    override fun deleteLikeById(id: Long): Post {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id/likes")
            .delete()
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let { gson.fromJson(it, Post::class.java) }
    }

    override fun shareById(id: Long) {
    }

    override fun removeById(id: Long) {
        Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id")
            .delete()
            .build()
            .let(client::newCall)
            .execute()
            .close()
    }

    override fun save(post: Post) {
        Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()
            .let(client::newCall)
            .execute()
            .close()
    }

    override fun insertDraft(content: String) {
        draftDao.insertDraft(DraftEntity.fromDtoDraft(content))
    }

    override fun deleteDraft() {
        draftDao.deleteDraft()
    }

    override fun getDraft(): String? {
        return draftDao.getDraft()
    }
}
