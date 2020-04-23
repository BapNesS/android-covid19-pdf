package fr.useless.depdero.ui.form

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.zxing.EncodeHintType
import fr.useless.depdero.core.model.Departure
import fr.useless.depdero.core.model.Reason
import fr.useless.depdero.core.model.User
import fr.useless.depdero.core.model.User.Companion.emptyInstance
import fr.useless.depdero.core.tool.Event
import fr.useless.depdero.core.tool.Resource
import fr.useless.depdero.core.tool.dateFrom
import fr.useless.depdero.pdf.Certificate
import fr.useless.depdero.storage.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.glxn.qrgen.android.QRCode
import java.text.SimpleDateFormat
import java.util.*

class FormViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository by lazy { UserRepository(application.applicationContext) }

    private val _user = MutableLiveData<Resource<User>>().apply { value = Resource.Loading() }
    val user : LiveData<Resource<User>> = _user

    private val _formSave = MutableLiveData<Resource<Boolean>>().apply { value = Resource.Loading() }
    val formSave : LiveData<Resource<Boolean>> = _formSave

    val departure = Departure().apply {
        date = Date()
    }
    private var creationDate = Date()

    val goodwillEvent = MutableLiveData<Event<Boolean>>().apply {this.value = Event(false) }

    private fun userValue() : User? {
        return user.value?.let {
            if (it is Resource.Success) {
                it.data
            } else null
        } ?: run { null }
    }

    init {
        // Charger le dernier stocké
        viewModelScope.launch {
            _user.postValue( userRepository.getUser() )
            _formSave.postValue( userRepository.getFormSaveOption() )

        }
        maxGenerationByDayReached {
            goodwillEvent.postValue( Event(true) )
        }

    }

    private fun maxGenerationByDayReached(doIt: () -> Unit) {
        userRepository.lastCreateFileDate()?.let {
            val cal = Calendar.getInstance().apply {
                this.add(Calendar.HOUR, -24)
            }
            if ( it.after( Date(cal.timeInMillis) ) ) {
                doIt()
            }
        }
    }

    fun resetUser() {
        _user.value = Resource.Success( emptyInstance() )
    }

    fun saveUser() {
        userValue()?.let { userRepository.saveUser( it ) }
    }

    fun saveFormSaveOption() {
        formSave.value?.let {
            if (it is Resource.Success) {
                userRepository.saveFormSaveOption( it.data )
            }
        }
    }

    fun putDatas(userName: String?,
                 userLastname: String?,
                 birthCity: String?,
                 birthDate: String?,
                 addressStreet: String?,
                 addressCity: String?,
                 addressZipcode: String?,
                 formReason: Reason?,
                 departureDate: String?,
                 departureHour: String?,
                 wantSaveDatas: Boolean
        ) {
        _formSave.value?.let {
            if (it is Resource.Success) {
                it.data = wantSaveDatas
            }
        }
        _user.value?.let {
            if (it is Resource.Success) {
                it.data.apply {
                    this.userName = userName.orEmpty()
                    this.userLastname = userLastname.orEmpty()
                    this.birthCity = birthCity.orEmpty()
                    this.birthDate = birthDate.orEmpty()
                    this.addressStreet = addressStreet.orEmpty()
                    this.addressCity = addressCity.orEmpty()
                    this.addressZipcode = addressZipcode.orEmpty()
                }
            }
        }

        departureDate?.let {
            departureHour?.let {
                val departureDateObj = dateFrom(departureDate, departureHour)
                departure.apply {
                    reason = formReason
                    date = departureDateObj
                }
            }
        }



    }

    private fun getQrSequence() : String {
        val formatter = SimpleDateFormat("dd/MM/yyyy 'a' HH:mm")
        val creationDateString = formatter.format(creationDate)
        val departureDate = departure.dateDay
        val departureHour = departure.dateHours

        return "Cree le: $creationDateString; Nom: ${userValue()?.userLastname}; Prenom: ${userValue()?.userName}; Naissance: ${userValue()?.birthDate} a ${userValue()?.birthCity}; Adresse: ${userValue()?.addressStreet} ${userValue()?.addressZipcode} ${userValue()?.addressCity}; Sortie: $departureDate a ${departureHour}; Motifs: ${departure.reason?.qrCodeValue}"
    }


    fun generateAll(context: Context, onComplete: (path: String) -> Unit) {
        creationDate = Date()
        (user.value as? Resource.Success)?.let { u ->
            viewModelScope.launch {
                val qrSeq = getQrSequence()
                val fullSizeQr = generateQRCode(qrSeq, 300).await()
                val thumbnailQr = generateQRCode(qrSeq, 100).await()
                val path: String = Certificate().apply {
                    reason = departure.reason
                    personName = u.data.displayName
                    birthCity = u.data.birthCity
                    birthDate = u.data.birthDate
                    streetAddress = u.data.addressStreet
                    zipCode = u.data.addressZipcode
                    city = u.data.addressCity
                    departureDate = departure.date ?: Date()
                }.generatePdfAsync(context,fullSizeQr, thumbnailQr, creationDate).await()
                saveLastCreateFileDate(creationDate)
                saveFilePath(path)
                onComplete(path)
            }
        } ?: run {
            onComplete("")
        }
    }

    private fun saveLastCreateFileDate(date: Date) {
        userRepository.addLastCreateFileDate(date)
    }

    private fun saveFilePath(path: String) {
        userRepository.lastFilePath = path
    }

    suspend fun generateQRCode(seq: String, dimension: Int = 300) = withContext(Dispatchers.IO) {
        async {
            QRCode.from(seq)
                .withSize(dimension, dimension)
                .withHint(EncodeHintType.MARGIN, 0)
                .bitmap()
        }
    }

}

