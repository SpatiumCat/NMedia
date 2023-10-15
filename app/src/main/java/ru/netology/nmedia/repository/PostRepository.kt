package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.FeedItem
import ru.netology.nmedia.Post
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>

    fun getNewer(id: Long): Flow<Int>
    suspend fun showAll()
//    suspend fun getAll()
    suspend fun likeById(id: Long)
    suspend fun deleteLikeById (id: Long)
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, photoModel: PhotoModel)
    suspend fun retrySaving(post: Post)
    suspend fun insertDraft(content: String)
    suspend fun deleteDraft()
    suspend fun getDraft(): String?

}
