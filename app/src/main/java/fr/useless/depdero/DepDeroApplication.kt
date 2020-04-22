package fr.useless.depdero

import android.app.Application
import fr.useless.depdero.notification.NotificationManager
import fr.useless.depdero.pdf.Certificate
import timber.log.Timber

class DepDeroApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        Certificate.loadPdfResources(this)

        NotificationManager(this).createChannels()
    }

}