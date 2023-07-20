package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST
import ru.netology.nmedia.Post
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.DraftEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import java.lang.Exception


//const val BASE_URL = "http://192.168.0.212:9999"

class PostRepositoryImpl(
    private val draftDao: DraftDao,
    private val postDao: PostDao
) : PostRepository {

    override val data: LiveData<List<Post>> = postDao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.map { it.copy(isSaved = true) }.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun likeById(id: Long) {
        val oldPost = data.value?.find { it.id == id }
        try {
            postDao.likeById(id)
            val response = PostApi.retrofitService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body.copy(isSaved = true)))
        } catch (e: IOException) {
            postDao.likeById(id)
            throw NetworkError
        } catch (e: Exception) {
            postDao.likeById(id)
            throw UnknownError
        }
    }

    override suspend fun deleteLikeById(id: Long) {
        try {
            postDao.likeById(id)
            val response = PostApi.retrofitService.deleteLikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body.copy(isSaved = true)))
        } catch (e: IOException) {
            postDao.likeById(id)
            throw NetworkError
        } catch (e: Exception) {
            postDao.likeById(id)
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val oldPost = data.value?.find { it.id == id }
        try {
            postDao.removeById(id)
            val response = PostApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            //val body = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            oldPost?.let { postDao.insert(PostEntity.fromDto(it)) }
            throw NetworkError
        } catch (e: Exception) {
            oldPost?.let { postDao.insert(PostEntity.fromDto(it)) }
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        postDao.shareById(id)
    }

    override suspend fun save(post: Post) {
        try {
            postDao.save(PostEntity.fromDto(post.copy(
                id = data.value?.maxOf { post -> post.id }?.plus(1) ?: 0,
                isSaved = false)))
            val response = PostApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeByContent(post.content)
            postDao.save(PostEntity.fromDto(body).copy(isSaved = true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun retrySaving(post: Post) {
        try {
            val response = PostApi.retrofitService.save(post.copy(id = 0))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeByContent(post.content)
            postDao.save(PostEntity.fromDto(body).copy(isSaved = true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun insertDraft(content: String) {
        draftDao.insertDraft(DraftEntity.fromDtoDraft(content))
    }

    override suspend fun deleteDraft() {
        draftDao.deleteDraft()
    }

    override suspend fun getDraft(): String? {
        return draftDao.getDraft()
    }
}
