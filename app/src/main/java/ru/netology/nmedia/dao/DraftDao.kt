package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DraftDao {

    @Query("UPDATE DraftEntity SET content = :content")
    fun saveDraft(content: String)

    @Query("SELECT content FROM DraftEntity")
    fun getDraft(): String
}
