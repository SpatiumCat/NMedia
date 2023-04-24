package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
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

    private fun changeLikedByMe(id: Long) {
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty().map {
                if (it.id == id) it.copy(likedByMe = !it.likedByMe) else it
            })
        )
    }

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
       repository.getAllAsync(object : PostRepository.GetAllCallback<List<Post>>{
           override fun onSuccess(posts: List<Post>) {
               _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
           }

           override fun onError(e: Exception) {
              _data.postValue(FeedModel(error = true))
           }
       })
    }

    fun save() {
        edited.value?.let {
                repository.save(it, object: PostRepository.GetAllCallback<Unit>{
                    override fun onSuccess(posts: Unit) {
                        _postCreated.postValue(Unit)
                    }

                    override fun onError(e: Exception) {
                        if (e is IOException) { throw IOException()}
                    }

                })
                _postCreated.postValue(Unit)
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
                    if (it.likedByMe) {
                        changeLikedByMe(id)
                        repository.deleteLikeById(id)
                    } else {
                        changeLikedByMe(id)
                        repository.likeById(id)
                    }
                } catch (e: IOException) {
                    _data.postValue(_data.value?.copy(posts = old))
                    return@thread
                }
            }
            post?.let {
                _data.postValue(FeedModel(posts = _data.value?.posts.orEmpty().map { post ->
                    if (post.id == it.id) it else post
                }))
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

