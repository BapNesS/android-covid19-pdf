package fr.useless.depdero.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.useless.depdero.storage.UserRepository


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository by lazy { UserRepository(application.applicationContext) }

    fun hasFile() = userRepository.lastFilePath.isNotEmpty()
    val lastFilePath = userRepository.lastFilePath

}
