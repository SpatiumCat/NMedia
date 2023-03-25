package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DraftEntity(
    @PrimaryKey
    val content: String
) {
}
