package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.Post
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    published = "",
    video = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
       AppDb.getInstance(application).draftDao()
    )
    private val _data = MutableLiveData<FeedModel>()
    val data: LiveData<FeedModel>
        get() = _data

    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    var draft = repository.getDraft() ?: ""

    init {
        loadPosts()
    }

    fun loadPosts () {
        _data.value = FeedModel(loading = true)
        thread {
            try {
                val posts = repository.getAll()
                FeedModel(empty = posts.isEmpty(), posts = posts)
            } catch (e: Exception) {
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
       saveDraft("")
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content == text) {
                return
            }
            edited.value = it.copy(content = text)
        }
    }

    fun likeById(id: Long) {
        thread {
            val old = _data.value?.posts.orEmpty()
            val post = old.find { it.id == id }?.let {
                try {
                    if(it.likedByMe) {
                        repository.deleteLikeById(id)
                    } else {
                        repository.likeById(id)
                    }
                } catch (e: IOException) {
                    _data.postValue(_data.value?.copy(posts = old))
                    return@thread
                }
            }
            post?.let {
                _data.postValue(_data.value
                    ?.copy(posts = old.map { post ->
                        if (post.id == it.id) it.copy(likedByMe = it.likedByMe) else post
                    })
                )
            }
        }
    }


    fun shareById(id: Long) {
        thread {
            repository.shareById(id)
        }
    }

    fun removeById(id: Long) {
        thread {
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty().filter { it.id != id })
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }

    fun saveDraft(content: String) {
        repository.deleteDraft()
        repository.insertDraft(content)
        draft = content
    }
    }

