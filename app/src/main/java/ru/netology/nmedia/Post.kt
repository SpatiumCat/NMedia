package ru.netology.nmedia

import ru.netology.nmedia.enums.AttachmentType

data class Post (
    val id: Long,
    val authorId: Long = 0L,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val video: String? = "",
    var likedByMe: Boolean = false,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val attachment: Attachment?,
    val isSaved: Boolean = false,
    val ownedByMe: Boolean = false
        )

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType
)

