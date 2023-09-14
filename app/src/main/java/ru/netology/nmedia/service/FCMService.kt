package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.Post
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushMessage
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {

        try {
            handlePushMessage(
                gson.fromJson(message.data[content], PushMessage::class.java)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        message.data[action]?.let {
            try {
                when (Action.valueOf(it)) {
                    Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
                    Action.POST -> handlePost(gson.fromJson(message.data[content], Post::class.java))
                }
            } catch (e: IllegalArgumentException) {
                return
            }
        }
    }

    override fun onNewToken(token: String) {
        println(token)
        appAuth.sendPushToken(token)
    }

    private fun handleLike(content: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.userName,
                    content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handlePost(post: Post) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(R.string.notification_user_posted, post.author)
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(post.content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun notifyPushMessage(content: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handlePushMessage(content: PushMessage) {
        val id = appAuth.data.value?.id
        content.recipientId?.let {
            when {
                it == id -> notifyPushMessage(content.content)
                else -> appAuth.sendPushToken()
            }
        } ?: notifyPushMessage(content.content)
    }
}

enum class Action {
    LIKE,
    POST,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)
