package ru.netology.nmedia.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostDaoImpl

class AppDb private constructor(db: SQLiteDatabase) {
    val postDao: PostDao = PostDaoImpl(db)

    companion object {
        @Volatile
        private var instance: AppDb? = null


        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: AppDb(
                    buildDatabase(context, arrayOf(PostDaoImpl.DDL_Post, PostDaoImpl.DDL_Draft))
                ).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context, DDLs: Array<String>) =
            DbHelper(context, 1, "app.db", DDLs).writableDatabase
    }

}

class DbHelper(context: Context, dbVersion: Int, dbName: String, private val DDLs: Array<String>) :
    SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        DDLs.forEach { db.execSQL(it) }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}
