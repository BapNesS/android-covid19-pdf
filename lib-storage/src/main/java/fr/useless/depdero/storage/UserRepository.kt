package fr.useless.depdero.storage

import android.content.Context
import android.text.TextUtils
import fr.useless.depdero.core.model.User
import fr.useless.depdero.core.tool.Constants.NB_MAX_ATTESTATION_BEFORE_WARNING
import fr.useless.depdero.core.tool.Constants.SEPARATOR_ATTESTATION_BEFORE_WARNING
import fr.useless.depdero.core.tool.LimitedSizeQueue
import fr.useless.depdero.core.tool.Resource
import java.util.*

class UserRepository(private val context: Context) {

    private val prefsAccess: PrefsAccess by lazy { PrefsAccess(context) }

    suspend fun getUser(): Resource<User> {
        return Resource.Success ( prefsAccess.user )
    }

    suspend fun getFormSaveOption(): Resource<Boolean> {
        return Resource.Success ( prefsAccess.formSaveOption )
    }

    fun saveUser(user: User) {
        prefsAccess.user = user
    }

    fun saveFormSaveOption(newValue: Boolean) {
        prefsAccess.formSaveOption = newValue
    }

    fun cleanDatas() {
        prefsAccess.cleanUser()
        prefsAccess.cleanFormSave()
    }

    fun addLastCreateFileDate(date: Date) {
        val queue = LimitedSizeQueue<String>(NB_MAX_ATTESTATION_BEFORE_WARNING)
        queue.addAllSafe(prefsAccess.lastCreatedFilesDate.split(SEPARATOR_ATTESTATION_BEFORE_WARNING))
        queue.addSafe(date.time.toString())
        prefsAccess.lastCreatedFilesDate = queue.toList().joinToString( SEPARATOR_ATTESTATION_BEFORE_WARNING )
    }

    fun lastCreateFileDate(): Date? {
        val queue = LimitedSizeQueue<String>(NB_MAX_ATTESTATION_BEFORE_WARNING)
        queue.addAllSafe(prefsAccess.lastCreatedFilesDate.split(SEPARATOR_ATTESTATION_BEFORE_WARNING))
        queue.lastMaxReached()?.let {
            if ( it.isNotEmpty() && TextUtils.isDigitsOnly(it) ) {
                return Date().apply {
                    time = it.toLong()
                }
            }
        }
        return null
    }

    var lastFilePath: String
    get() = prefsAccess.lastFilePath
    set(value) { prefsAccess.lastFilePath = value }

}