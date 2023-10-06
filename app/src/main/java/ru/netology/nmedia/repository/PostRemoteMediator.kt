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
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val apiService: PostApiService,
    private val appDb: AppDb,
    private val postRemoteKeyDao: PostRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    private val postDao = appDb.postDao()


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id == null) {
                        apiService.getLatest(state.config.pageSize)
                    } else {
                        apiService.getAfter(id, state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(false)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.code(), result.message())
            }
            val body = result.body() ?: throw ApiError(result.code(), result.message())

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                        if (postRemoteKeyDao.getAll().isEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        }

//                        postRemoteKeyDao.insert(
//                            listOf(
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.AFTER,
//                                    body.first().id
//                                ),
//                                PostRemoteKeyEntity(
//                                    PostRemoteKeyEntity.KeyType.BEFORE,
//                                    body.last().id
//                                )
//                            )
//                        )
                    }

                    LoadType.PREPEND -> {
                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                }
                postDao.insert(body.map { it.copy(isSaved = true) }.toEntity(hidden = false))
            }

            return MediatorResult.Success(body.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }
}
