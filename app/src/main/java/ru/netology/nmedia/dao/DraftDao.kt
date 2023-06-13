package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.DraftEntity

@Dao
interface DraftDao {

    @Insert
    suspend fun insertDraft(content: DraftEntity)

    @Query("DELETE FROM DraftEntity")
    suspend fun deleteDraft()

//    @Query("UPDATE DraftEntity SET content = :content")
//    fun saveDraft(content: String)

    @Query("SELECT content FROM DraftEntity")
    suspend fun getDraft(): String?
}
