package ru.netology.nmedia.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.AuthApiService
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject

class SendPushWorker @Inject constructor(
    private val context: Context,
    params: WorkerParameters,

): CoroutineWorker(
    context,
    params
) {
    companion object {
        const val NAME = "SendPushWorker"
        const val TOKEN_KEY = "TOKEN_KEY"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SendPushWorkerEntryPoint {
        fun getAuthApiService(): AuthApiService
    }
    private val entryPoint = EntryPointAccessors.fromApplication(
        context,
        SendPushWorkerEntryPoint::class.java
    )
    override suspend fun doWork(): Result {
        val token = inputData.getString(TOKEN_KEY)

        val pushToken = PushToken(token ?: Firebase.messaging.token.await())

        return runCatching {
            entryPoint.getAuthApiService().sendPushToken(pushToken)
//            AuthApi.retrofitService.sendPushToken(pushToken)
        }.map {
             Result.success()
        }.getOrElse {
             Result.retry()
        }
    }
}
