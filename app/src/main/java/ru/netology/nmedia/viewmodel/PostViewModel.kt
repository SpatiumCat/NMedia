package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImp

class PostViewModel: ViewModel() {

    private val repository: PostRepository = PostRepositoryInMemoryImp()
    val data = repository.getAll()

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
}
