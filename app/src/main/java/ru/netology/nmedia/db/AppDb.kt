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

@Database(entities = [DraftEntity::class, PostEntity::class], version = 7)
abstract class AppDb : RoomDatabase() {
    abstract fun draftDao(): DraftDao
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null


        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java,  "app.db")
                .fallbackToDestructiveMigration()
                //.allowMainThreadQueries()
                .build()
    }

}

