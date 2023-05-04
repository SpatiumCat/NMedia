package ru.netology.nmedia

import ru.netology.nmedia.enums.AttachmentType

data class Post (
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val video: String? = "",
    var likedByMe: Boolean = false,
    val likes: Int = 1099,
    val shares: Int = 1000,
    val views: Int = 12500,
    val attachment: Attachment?
        )

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType
)

