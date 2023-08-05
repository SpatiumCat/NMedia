package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.Attachment
import ru.netology.nmedia.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val video: String? = "",
    var likedByMe: Boolean = false,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,

    @Embedded
    val attachment: Attachment?,
    val isSaved: Boolean = false,
    val hidden: Boolean = false,
    ) {
    fun toDto() = Post(id, author, authorAvatar, content, published, video, likedByMe, likes, shares, views, attachment, isSaved)

    companion object {
        fun fromDto(dto: Post, hidden: Boolean) =
            if(dto.attachment == null) {
                PostEntity(
                    dto.id,
                    dto.author,
                    dto.authorAvatar,
                    dto.content,
                    dto.published,
                    dto.video,
                    dto.likedByMe,
                    dto.likes,
                    dto.shares,
                    dto.views,
                    null,
                    dto.isSaved,
                    hidden = hidden
                )
            } else {
                PostEntity(
                    dto.id,
                    dto.author,
                    dto.authorAvatar,
                    dto.content,
                    dto.published,
                    dto.video,
                    dto.likedByMe,
                    dto.likes,
                    dto.shares,
                    dto.views,
                    dto.attachment,
                    dto.isSaved,
                    hidden = hidden
                )
            }

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> = map { PostEntity.fromDto(it, hidden) }
