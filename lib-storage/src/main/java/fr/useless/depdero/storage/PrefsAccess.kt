package fr.useless.depdero.storage

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import fr.useless.depdero.core.model.User
import java.util.*

internal const val PREFS_NAME = "PrefsName"

internal class PrefsAccess(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    var lastCreatedFilesDate : String
        get() = sharedPreferences.getString(StringKeys.LIST_CREATED_FILES_DATE.name, "").orEmpty()
        set(value) = sharedPreferences.edit { putString(StringKeys.LIST_CREATED_FILES_DATE.name, value) }

    var lastFilePath : String
        get() = sharedPreferences.getString(StringKeys.FILE_PATH.name, "").orEmpty()
        set(value) {
            sharedPreferences.edit {
                putString( StringKeys.FILE_PATH.name, value )
                putLong( LongKeys.FILE_DATE.name, Date().time )
            }
        }

    var formSaveOption : Boolean
        get() = sharedPreferences.getBoolean(BooleanKeys.FORM_SAVE.name, true)
        set(value) = sharedPreferences.edit { putBoolean( BooleanKeys.FORM_SAVE.name, value ) }

    var user: User
        get() = User(
                    sharedPreferences.getString(StringKeys.USER_NAME.name, "").orEmpty(),
                    sharedPreferences.getString(StringKeys.USER_LASTNAME.name, "").orEmpty(),
                    sharedPreferences.getString(StringKeys.BIRTH_CITY.name, "").orEmpty(),
                    sharedPreferences.getString(StringKeys.BIRTH_DATE.name, "").orEmpty(),
                    sharedPreferences.getString(StringKeys.ADDRESS_STREET.name, "").orEmpty(),
                    sharedPreferences.getString(StringKeys.ADDRESS_CITY.name, "").orEmpty(),
                    sharedPreferences.getString(StringKeys.ADDRESS_ZIPCODE.name, "").orEmpty()
                )
        set(value) {
            sharedPreferences.edit {
                putString( StringKeys.USER_NAME.name, value.userName )
                putString( StringKeys.USER_LASTNAME.name, value.userLastname )
                putString( StringKeys.BIRTH_CITY.name, value.birthCity )
                putString( StringKeys.BIRTH_DATE.name, value.birthDate )
                putString( StringKeys.ADDRESS_STREET.name, value.addressStreet )
                putString( StringKeys.ADDRESS_CITY.name, value.addressCity )
                putString( StringKeys.ADDRESS_ZIPCODE.name, value.addressZipcode )
            }
        }

    fun cleanUser() {
        sharedPreferences.edit {
            StringKeys.values().filter {
                it.userData
            }.forEach {
                remove( it.name )
            }
        }
    }

    fun cleanFormSave() {
        sharedPreferences.edit {
            remove( BooleanKeys.FORM_SAVE.name )
        }
    }

    var prefersNpa
        get() = sharedPreferences.getBoolean(BooleanKeys.PREFERS_NON_PERSON_ADS.name, false)
        set(value) = sharedPreferences.edit { putBoolean(BooleanKeys.PREFERS_NON_PERSON_ADS.name, value)}

    enum class StringKeys(val userData: Boolean) {
        USER_NAME(true),
        USER_LASTNAME(true),
        BIRTH_CITY(true),
        BIRTH_DATE(true),
        ADDRESS_STREET(true),
        ADDRESS_CITY(true),
        ADDRESS_ZIPCODE(true),

        FILE_PATH(false),
        LIST_CREATED_FILES_DATE(false),

    }

    enum class BooleanKeys {
        FORM_SAVE,
        PREFERS_NON_PERSON_ADS
    }

    enum class LongKeys {
        FILE_DATE
    }

}