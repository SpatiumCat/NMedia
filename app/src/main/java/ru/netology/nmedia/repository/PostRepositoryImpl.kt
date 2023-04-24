package ru.netology.nmedia.repository


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.Post
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.entity.DraftEntity
import java.io.IOException
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


//    override fun getAll(): List<Post> {
//        val request: Request = Request.Builder()
//            .url("$BASE_URL/api/slow/posts").build()
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let { gson.fromJson(it, typeToken.type) }
//    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: throw RuntimeException("body is null")
                try {
                    callback.onSuccess(gson.fromJson(body, typeToken.type))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }
        })
    }


    override fun likeById(id: Long): Post {
        return Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id/likes")
            .post(gson.toJson(id).toRequestBody(jsonType))
            .build().let(client::newCall)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let { gson.fromJson(it, Post::class.java) }
    }

    override fun deleteLikeById(id: Long): Post {
        return Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id/likes")
            .delete()
            .build().let(client::newCall)
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

    override fun save(post: Post, callback: PostRepository.GetAllCallback<Unit>) {
        Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()
            .let(client::newCall)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                   callback.onSuccess(Unit)
                }
            })
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
