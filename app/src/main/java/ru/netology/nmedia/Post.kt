package ru.netology.nmedia

data class Post (
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val video: String = "",
    var likedByMe: Boolean = false,
    val likes: Int = 1099,
    val shares: Int = 1000,
    val views: Int = 12500
        )

