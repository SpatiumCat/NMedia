package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
//    fun saveDraft(content: String)
//    fun insertDraft(content: String)
//    fun deleteDraft()
//    fun getDraft(): String?
}
