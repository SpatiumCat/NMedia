package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.workers.SendPushWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY = "TOKEN_KEY"
    private val ID_KEY = "ID_KEY"
    private val AVATAR_KEY = "AVATAR_KEY"
//    companion object {
//        private const val TOKEN_KEY = "TOKEN_KEY"
//        private const val ID_KEY = "ID_KEY"
//        private const val AVATAR_KEY = "AVATAR_KEY"
//
//        @Volatile
//        private var INSTANCE: AppAuth? = null
//        fun initApp(context: Context) {
//            INSTANCE = AppAuth(context)
//        }
//
//        fun getInstance(): AppAuth = requireNotNull(INSTANCE) {
//            "You must call initApp before"
//        }
//    }

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

    fun sendPushToken(token: String? = null) {

        WorkManager.getInstance(context).enqueueUniqueWork(
            SendPushWorker.NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SendPushWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putString(SendPushWorker.TOKEN_KEY, token)
                        .build()
                )
                .build()
        )

//        CoroutineScope(Dispatchers.Default).launch {
//            val pushToken = PushToken(token ?: Firebase.messaging.token.await())
//
//            runCatching {
//                AuthApi.retrofitService.sendPushToken(pushToken)
//            }
//        }
    }

    @Synchronized
    fun setToken(token: Token) {
        println("token:${token.token}\nid:${token.id}")
        _data.value = token
        prefs.edit {
            putString(TOKEN_KEY, token.token)
            putLong(ID_KEY, token.id)
            putString(AVATAR_KEY, token.avatar)
        }
        sendPushToken()
    }

    @Synchronized
    fun clearAuth() {
        _data.value = null
        prefs.edit { clear() }

        sendPushToken()
    }
}
