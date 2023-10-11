package ru.netology.nmedia.repository


import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertFooterItem
import androidx.paging.insertHeaderItem
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.Ad
import ru.netology.nmedia.Attachment
import ru.netology.nmedia.FeedItem
import ru.netology.nmedia.Loading
import ru.netology.nmedia.Post
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.entity.DraftEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enums.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


//const val BASE_URL = "http://192.168.0.212:9999"
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val draftDao: DraftDao,
    private val postDao: PostDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    @ApplicationContext private val context: Context
) : PostRepository {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PostRepositoryImplEntryPoint {
        fun getPostApiService(): PostApiService
        fun getAppAuth(): AppAuth
        fun getAppDb(): AppDb
    }

    private val entryPoint = EntryPointAccessors.fromApplication(
        context,
        PostRepositoryImplEntryPoint::class.java
    )

    private var posts = emptyList<Post>()


    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 5, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = entryPoint.getPostApiService(),
            appDb = entryPoint.getAppDb(),
            postRemoteKeyDao = postRemoteKeyDao,
        )
    ).flow.map {
        it.map(PostEntity::toDto)
            .insertSeparators { previous, _ ->
                if (previous?.id?.rem(5) == 0L) {
                    Ad(Random.nextLong(), "figma.jpg")
                } else { null }
            }.insertFooterItem(item = Loading(Random.nextLong()))
            .insertHeaderItem(item = Loading(Random.nextLong()))
    }


    override fun getNewer(id: Long): Flow<Int> = flow {

        while (true) {
            delay(10_000)

            try {
                val response = entryPoint.getPostApiService().getNewer(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                postDao.insert(body.map { it.copy(isSaved = true) }.toEntity(hidden = true))
                emit(body.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                throw NetworkError
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

//    override suspend fun getAll() {
//        try {
//            val response = entryPoint.getPostApiService().getAll()
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            postDao.insert(body.map { it.copy(isSaved = true) }.toEntity())
//        } catch (e: IOException) {
//            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
//    }


    override suspend fun likeById(id: Long) {
        val oldPost = posts.find { it.id == id }
        try {
            postDao.likeById(id)
            val response = entryPoint.getPostApiService().likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body.copy(isSaved = true), hidden = false))
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
            val response = entryPoint.getPostApiService().deleteLikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body.copy(isSaved = true), hidden = false))
        } catch (e: IOException) {
            postDao.likeById(id)
            throw NetworkError
        } catch (e: Exception) {
            postDao.likeById(id)
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {

        val oldPost = posts.find { it.id == id }
        try {
            postDao.removeById(id)
            val response = entryPoint.getPostApiService().removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            //val body = response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            oldPost?.let { postDao.insert(PostEntity.fromDto(it, hidden = false)) }
            throw NetworkError
        } catch (e: Exception) {
            oldPost?.let { postDao.insert(PostEntity.fromDto(it, hidden = false)) }
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        postDao.shareById(id)
    }

    override suspend fun save(post: Post) {

        try {
            postDao.save(
                PostEntity.fromDto(
                    post.copy(
                        id = posts.maxOfOrNull { post -> post.id }?.plus(1) ?: 0,
                        isSaved = false
                    ), hidden = false
                )
            )
            val response = entryPoint.getPostApiService().save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeByContent(post.content)
            postDao.save(PostEntity.fromDto(body.copy(isSaved = true), hidden = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel) {

        try {
            val media = uploadMedia(photoModel)

            postDao.save(
                PostEntity.fromDto(
                    post.copy(
                        id = posts.maxOfOrNull { post -> post.id }?.plus(1) ?: 0,
                        isSaved = false,
                        attachment = Attachment(media.id, null, AttachmentType.IMAGE)
                    ), hidden = false
                )
            )
            val response = entryPoint.getPostApiService().save(
                post.copy(
                    attachment = Attachment(
                        media.id,
                        null,
                        AttachmentType.IMAGE
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeByContent(post.content)
            postDao.save(PostEntity.fromDto(body.copy(isSaved = true), hidden = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun uploadMedia(model: PhotoModel): Media {
        val response = entryPoint.getPostApiService().uploadMedia(
            MultipartBody.Part.createFormData("file", "file", model.file.asRequestBody())
        )
        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }
        return requireNotNull(response.body())
    }

    override suspend fun retrySaving(post: Post) {

        try {
            val response = entryPoint.getPostApiService().save(post.copy(id = 0))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.removeByContent(post.content)
            postDao.save(PostEntity.fromDto(body.copy(isSaved = true), hidden = false))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun showAll() {
        postDao.showAll()
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
