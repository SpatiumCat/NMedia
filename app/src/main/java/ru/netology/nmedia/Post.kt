package ru.netology.nmedia

class Post (
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likedByMe: Boolean = false,
    var likes: Int = 1499,
    var shares: Int = 1000,
    var views: Int = 12500
        ) {
}
