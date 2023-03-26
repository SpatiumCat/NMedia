package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.DraftEntity

@Dao
interface DraftDao {

    @Insert
    fun insertDraft(content: DraftEntity)

    @Query("DELETE FROM DraftEntity")
    fun deleteDraft()

//    @Query("UPDATE DraftEntity SET content = :content")
//    fun saveDraft(content: String)

    @Query("SELECT content FROM DraftEntity")
    fun getDraft(): String?
}
