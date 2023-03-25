package ru.netology.nmedia.repository

import androidx.lifecycle.*
import ru.netology.nmedia.Post
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity


class PostRepositoryImpl(private val postDao: PostDao, private val draftDao: DraftDao) : PostRepository {



    override fun getAll() = postDao.getAll().map { list -> list.map { it.toDto() } }


    override fun likeById(id: Long) {
        postDao.likeById(id)
    }

    override fun shareById(id: Long) {
        postDao.shareById(id)
    }

    override fun removeById(id: Long) {
        postDao.removeById(id)
    }

    override fun save(post: Post) {
      postDao.save(PostEntity.fromDto(post))
    }

    override fun saveDraft(content: String) {
        draftDao.saveDraft(content)
    }

    override fun getDraft(): String {
        return draftDao.getDraft()
    }
}
