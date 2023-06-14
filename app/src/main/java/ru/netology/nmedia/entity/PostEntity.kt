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
    val likes: Int = 1099,
    val shares: Int = 1000,
    val views: Int = 12500,
    @Embedded
    val attachment: Attachment?
    ) {
    fun toDto() = Post(id, author, authorAvatar, content, published, video, likedByMe, likes, shares, views, attachment)

    companion object {
        fun fromDto(dto: Post) =
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
                    null
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
                    dto.attachment
                )
            }

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
