package ru.netology.nmedia.repository

import ru.netology.nmedia.Post

interface PostRepository {
//    fun getAll(): List<Post>
    fun getAllAsync(callback: GetAllCallback<List<Post>>)
    fun likeById(id: Long): Post
    fun deleteLikeById (id: Long): Post
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post, callback: GetAllCallback<Unit>)
    fun insertDraft(content: String)
    fun deleteDraft()
    fun getDraft(): String?

    interface GetAllCallback<T>{
        fun onSuccess(posts: T)
        fun onError(e: Exception)
    }
}
