package fr.useless.depdero.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.security.SecureRandom

class NotificationManager(private val context: Context?) {

    fun createChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.let {
                DepDeroNotificationChannel.values().forEach {
                    val name = context.getString(it.title)
                    val descriptionText = context.getString(it.description)
                    val importance = it.importance
                    val channel = NotificationChannel(it.id, name, importance).apply {
                        description = descriptionText
                    }
                    // Register the channel with the system
                    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }
    }

    fun generateFileNotification(path: String, intent: Intent) {
        context?.let {
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            var builder = NotificationCompat.Builder(context, DepDeroNotificationChannel.GENERATED_FILE.id)
                .setSmallIcon( R.drawable.ic_description_rounded )
                .setContentTitle( context.getString(R.string.notification_title) )
                .setContentText( context.getString(R.string.notification_short_description)+" $path" )
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText( context.getString(R.string.notification_long_description)+path )
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with( NotificationManagerCompat.from( context ) ) {
                notify( SecureRandom().nextInt(), builder.build())
            }
        }
    }
}