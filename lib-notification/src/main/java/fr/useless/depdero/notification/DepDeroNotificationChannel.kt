package fr.useless.depdero.notification

import android.app.NotificationManager
import androidx.annotation.StringRes

enum class DepDeroNotificationChannel(val id: String,
                                     @StringRes val title: Int,
                                     @StringRes val description: Int,
                                     val importance: Int){

    GENERATED_FILE("generate_file", R.string.channel_name, R.string.channel_description, NotificationManager.IMPORTANCE_MAX)

}