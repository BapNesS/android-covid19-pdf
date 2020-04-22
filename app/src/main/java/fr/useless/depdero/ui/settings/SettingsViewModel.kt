package fr.useless.depdero.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.useless.depdero.storage.UserRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository by lazy { UserRepository(application.applicationContext) }

    fun cleanDatas() { userRepository.cleanDatas() }

}