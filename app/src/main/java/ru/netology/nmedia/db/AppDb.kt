package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.DraftEntity
import ru.netology.nmedia.entity.PostEntity

@Database(entities = [DraftEntity::class, PostEntity::class], version = 12)
abstract class AppDb : RoomDatabase() {
    abstract fun draftDao(): DraftDao
    abstract fun postDao(): PostDao

}

