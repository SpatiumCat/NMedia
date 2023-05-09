package ru.netology.nmedia.repository

import ru.netology.nmedia.Post

interface PostRepository {
//    fun getAll(): List<Post>
    fun getAllAsync(callback: GetAllCallback<List<Post>>)
    fun likeByIdAsync(id: Long, callback: GetAllCallback<Post>)
    fun deleteLikeByIdAsync (id: Long, callback: GetAllCallback<Post>)
    fun shareById(id: Long)
    fun removeByIdAsync(id: Long, callback: GetAllCallback<Unit>)
    fun saveAsync(post: Post, callback: GetAllCallback<Post>)
    fun insertDraft(content: String)
    fun deleteDraft()
    fun getDraft(): String?

    interface GetAllCallback<T>{
        fun onSuccess(post_s: T)
        fun onError(e: Exception)
    }
}
