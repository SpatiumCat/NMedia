package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val video: String = "",
    var likedByMe: Boolean = false,
    val likes: Int = 1099,
    val shares: Int = 1000,
    val views: Int = 12500
    ) {
    fun toDto() = Post(id, author, content, published, video, likedByMe, likes, shares, views)

    companion object{
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.content,
                dto.published,
                dto.video,
                dto.likedByMe,
                dto.likes,
                dto.shares,
                dto.views
            )

    }
}
