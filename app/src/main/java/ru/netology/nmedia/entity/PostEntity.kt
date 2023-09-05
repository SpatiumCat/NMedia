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
    val authorId: Long,
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
    fun toDto() = Post(
        id = id,
        author = author,
        authorId = authorId,
        authorAvatar = authorAvatar,
        content = content,
        published = published,
        video = video,
        likedByMe = likedByMe,
        likes = likes,
        shares = shares,
        views = views,
        attachment = attachment,
        isSaved = isSaved
    )

    companion object {
        fun fromDto(dto: Post, hidden: Boolean) =
            if (dto.attachment == null) {
                PostEntity(
                    id = dto.id,
                    author = dto.author,
                    authorId = dto.authorId,
                    authorAvatar = dto.authorAvatar,
                    content = dto.content,
                    published = dto.published,
                    video = dto.video,
                    likedByMe = dto.likedByMe,
                    likes = dto.likes,
                    shares = dto.shares,
                    views = dto.views,
                    attachment = null,
                    isSaved = dto.isSaved,
                    hidden = hidden
                )
            } else {
                PostEntity(
                    id = dto.id,
                    author = dto.author,
                    authorId = dto.authorId,
                    authorAvatar = dto.authorAvatar,
                    content = dto.content,
                    published = dto.published,
                    video = dto.video,
                    likedByMe = dto.likedByMe,
                    likes = dto.likes,
                    shares = dto.shares,
                    views = dto.views,
                    attachment = dto.attachment,
                    isSaved = dto.isSaved,
                    hidden = hidden
                )
            }

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(hidden: Boolean = false): List<PostEntity> =
    map { PostEntity.fromDto(it, hidden) }
