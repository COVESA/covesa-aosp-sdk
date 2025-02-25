package global.covesa.sdk.client.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import global.covesa.sdk.client.R
import java.util.concurrent.ThreadLocalRandom

class Notification(private val context: Context) {
    private val channelId = context.packageName
    private val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun showNotification(title: String, text: String) {
        val notificationBuilder = Notification.Builder(context, channelId)
        val notification =
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(text)
                .build()

        val notificationId = ThreadLocalRandom.current().nextInt()
        nManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val name = context.packageName
        val descriptionText = "Show test notification"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        nManager.createNotificationChannel(channel)
    }
}