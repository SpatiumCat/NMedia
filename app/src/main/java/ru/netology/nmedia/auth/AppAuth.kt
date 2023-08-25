package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token

class AppAuth private constructor(
    context: Context
) {
    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
        private const val AVATAR_KEY = "AVATAR_KEY"
        @Volatile
        private var INSTANCE: AppAuth? = null

        fun initApp(context: Context) {
            INSTANCE = AppAuth(context)
        }

        fun getInstance(): AppAuth = requireNotNull(INSTANCE) {
            "You must call initApp before"
        }
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data = MutableStateFlow<Token?>(null)
    val data = _data.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)
        val avatar = prefs.getString(AVATAR_KEY, null)

        if (token == null || id == 0L) {
            prefs.edit { clear() }
        } else {
            _data.value = Token(id, token, avatar)
        }
    }

    @Synchronized
    fun setToken(token: Token) {
        _data.value = token
        prefs.edit {
            putString(TOKEN_KEY, token.token)
            putLong(ID_KEY, token.id)
            putString(AVATAR_KEY, token.avatar )
        }

    }

    @Synchronized
    fun clearAuth() {
        _data.value = null
        prefs.edit { clear() }
    }
}
