package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.netology.nmedia.Post
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    published = 0,
    video = "",
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(application).draftDao(),
        AppDb.getInstance(application).postDao()
    )

    val data: LiveData<FeedModel> = repository.data.map { FeedModel(posts = it) }
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    var draft: String = ""


//    private fun changeLikedByMe(id: Long) {
//        data.value = data.value?.copy(posts = data.value?.posts.orEmpty().map {
//            if (it.id == id) it.copy(likedByMe = !it.likedByMe) else it
//        }
//        )
//    }

//    private fun updatePosts(updatedPost: Post) {
//        data.value = FeedModel(posts = data.value?.posts.orEmpty().map { oldPost ->
//            if (oldPost.id == updatedPost.id) updatedPost else oldPost
//        })
//    }


    init {
        loadPosts()
        loadDraft()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(refreshing = true)
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            try {
                edited.value?.let {
                    _postCreated.value = Unit
                    repository.save(it.copy(isSaved = false))
                }
                edited.value = empty
                saveDraft("")
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
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
            edited.value = it.copy(
                content = text,
            )
        }
    }

    fun likeById(id: Long) {
        val oldPost = data.value?.posts?.find { it.id == id }?.let {
            if (it.likedByMe) {
                viewModelScope.launch {
                    try {
                        repository.deleteLikeById(id)
                    } catch (e: Exception) {
                        _dataState.value = FeedModelState(error = true)

                    }
                }
            } else {
                viewModelScope.launch {
                    try {
                        repository.likeById(id)
                    } catch (e: Exception) {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }

    }


    fun shareById(id: Long) {
        viewModelScope.launch {
            repository.shareById(id)
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    private fun loadDraft() {
        val draftDeferred = viewModelScope.launch { draft = repository.getDraft() ?: "" }
        //viewModelScope.launch { draft = draftDeferred.await() ?: "" }
    }

    fun saveDraft(content: String) {
        viewModelScope.launch {
            repository.deleteDraft()
            repository.insertDraft(content)
            draft = content
        }
    }
}



