package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.Post
import ru.netology.nmedia.dao.PostDao


class PostRepositorySQLiteImpl(private val dao: PostDao) : PostRepository {

    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)
    private var draft = ""

    init {
        posts = dao.getAll()
        data.value = posts
//        if (!dao.getDraft().isNullOrBlank()) {
//            draft = dao.getDraft()
//        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {

        dao.likeById(id)
        posts = posts.map {
            if (id != it.id) it
            else it.copy(likedByMe = !it.likedByMe, likes = it.likes + if (it.likedByMe) -1 else +1)
        }
        data.value = posts
    }

    override fun shareById(id: Long) {

        dao.shareById(id)
        posts = posts.map {
            if (id != it.id) it else it.copy(shares = it.shares + 10)
        }
        data.value = posts
    }

    override fun removeById(id: Long) {

        dao.removeById(id)
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post) {
        val id = post.id
        val saved = dao.save(post)
        posts = if (id == 0L) {
            listOf(saved) + posts
        } else {
            posts.map {
                if (it.id != id) it else saved
            }
        }
        data.value = posts
    }

    override fun saveDraft(content: String) {
            draft = dao.saveDraft(content)
    }

    override fun getDraft(): String {
        return draft
    }
}
