package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.Post
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val count: Int,
    private val apiService: PostApiService,
    private val appDb: AppDb
) : RemoteMediator<Long, Post>() {

    private val postDao = appDb.postDao()

    override suspend fun load(loadType: LoadType, state: PagingState<Long, Post>): MediatorResult {
        return try {

            val result = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatest(count)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.APPEND -> {
                    apiService.getBefore(state.lastItemOrNull()?.id ?: 1L, count)
                }

            }


            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }
            val body = result.body() ?: throw ApiError(result.code(), result.message())

            appDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    postDao.clearAll()
                }
                postDao.insert(body.map { it.copy(isSaved = true) }.toEntity(hidden = true))
            }

            MediatorResult.Success(
                endOfPaginationReached = body.lastOrNull() == null
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}
