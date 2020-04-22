package fr.useless.depdero.ui.form

import android.Manifest
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import fr.useless.depdero.MainActivity
import fr.useless.depdero.R
import fr.useless.depdero.core.model.Departure
import fr.useless.depdero.core.model.Reason
import fr.useless.depdero.core.tool.*
import fr.useless.depdero.notification.NotificationManager
import fr.useless.depdero.utils.Constants.MY_PERMISSIONS_REQUEST_CODE
import kotlinx.android.synthetic.main.form_fragment.*
import timber.log.Timber
import java.util.*


class FormFragment : Fragment() {

    private val notificationManager: NotificationManager by lazy { NotificationManager( this.context ) }

    private lateinit var viewModel: FormViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.form_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FormViewModel::class.java)

        initViews()
        initObservers()
        initListeners()
    }

    private fun initViews() {
        radioTravail.setText(Reason.TRAVAIL.description)
        radioCourses.setText(Reason.COURSES.description)
        radioSante.setText(Reason.SANTE.description)
        radioFamille.setText(Reason.FAMILLE.description)
        radioSport.setText(Reason.SPORT.description)
        radioJudiciaire.setText(Reason.JUDICIAIRE.description)
        radioMissions.setText(Reason.MISSIONS.description)

        departureDateEt.editText?.setText( viewModel.departure.dateDay )
        departureHourEt.editText?.setText( viewModel.departure.dateHours )

        setRadioSelection(viewModel.departure)
    }


    private fun initListeners() {
        arrayListOf(R.id.radioCourses, R.id.radioFamille,
                         R.id.radioJudiciaire, R.id.radioMissions,
                         R.id.radioSante, R.id.radioSport,
                         R.id.radioTravail).forEach {
            this.view?.findViewById<RadioButton>(it)?.setOnClickListener {
                this.hideSoftKeyboard()
            }
        }

        resetBtn.setOnClickListener {
            viewModel.resetUser()
        }

        generateBtn.setOnClickListener {

            // Here, it is the current context
            if (ContextCompat.checkSelfPermission( this.requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if ( shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                    rootLayout?.showSnackbar(R.string.store_permission_required)
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_CODE)

                } else {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_CODE)
                }

            } else {
                // Permission has already been granted
                launchGenerationProcess()
            }

        }

        birthDateEt.editText?.setOnClickListener {
            this.hideSoftKeyboard()

            val triple = formatStringToYmd ( (it as EditText).text.toString() )

            val currentDate: Calendar = Calendar.getInstance()
            val year = triple?.first ?: currentDate.get(Calendar.YEAR)
            val month = triple?.second ?: currentDate.get(Calendar.MONTH)
            val day = triple?.third ?: currentDate.get(Calendar.DAY_OF_MONTH)

            this.context?.let { context ->
                val datePickerDialog = DatePickerDialog(
                    context,
                    OnDateSetListener { _, year, month, day ->
                        Timber.d("year: $year month: $month day: $day")
                        birthDateEt.quickSet( formatYmdToString(year, month+1, day) )
                    },
                    year, month, day
                )
                if ( triple == null ) {
                    // Open on year selection first
                    try {
                        datePickerDialog.datePicker.touchables[0].performClick()
                    } catch ( ex: Exception ) {
                        Timber.e("Easy, but not peasy")
                    }
                }
                datePickerDialog.show()
            }
        }

        departureDateEt.editText?.setOnClickListener {
            this.hideSoftKeyboard()

            val triple = formatStringToYmd ( (it as EditText).text.toString() )

            val currentDate: Calendar = Calendar.getInstance()
            val year = triple?.first ?: currentDate.get(Calendar.YEAR)
            val month = triple?.second ?: currentDate.get(Calendar.MONTH)
            val day = triple?.third ?: currentDate.get(Calendar.DAY_OF_MONTH)

            this.context?.let { context ->
                val datePickerDialog = DatePickerDialog(
                    context,
                    OnDateSetListener { _, year, month, day ->
                        Timber.d("year: $year month: $month day: $day")
                        departureDateEt.quickSet( formatYmdToString(year, month+1, day) )
                    },
                    year, month, day
                )
                datePickerDialog.show()
            }
        }

        departureHourEt.editText?.setOnClickListener {
            this.hideSoftKeyboard()

            this.context?.let { context ->
                val currentDate = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    TimePickerDialog.OnTimeSetListener { _ , hours, minutes ->
                        departureHourEt.quickSet(formatHhmmToString(hours, minutes))
                    },
                    currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }
    }

    private fun launchGenerationProcess() {
        generateBtn.isEnabled = false
        generateBtn.text = context?.getString(R.string.btn_generating)

        scrollview.smoothScrollTo( generateBtn.left, generateBtn.bottom )

        putDatas()

        checkDatas (
            onSuccess = {
                // Sauver si nécessaire
                if ( wantSaveDatas.isChecked ) {
                    viewModel.saveUser()
                }
                viewModel.saveFormSaveOption()

                viewModel.generateAll(requireContext()) { path ->

                    context?.getShowPdfFileIntent( path )?.let { intent ->
                        notificationManager.generateFileNotification(path, intent)
                        createDynamicShortcut(intent)
                    }

                    findNavController().navigate(R.id.action_formFragment_to_mainFragment)
                }
            },
            onError = {
                // TODO mettre en valeur les fields en erreur
                rootLayout?.showSnackbar(R.string.error_all_fields_required)
                generateBtn.isEnabled = true
                generateBtn.text = context?.getString(R.string.btn_generate)
            }
        )
    }

    private fun createDynamicShortcut(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = this.activity?.getSystemService(ShortcutManager::class.java)

            val shortcut = ShortcutInfo.Builder(context, "dynamicLastFile")
                .setShortLabel( getString(R.string.home_lastfile) )
                .setLongLabel( getString(R.string.home_lastfile) )
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_shortcut_lastfile))
                .setIntent(intent)
                .build()
            shortcutManager?.dynamicShortcuts = Arrays.asList(shortcut)
        }
    }

    private fun checkDatas(onSuccess: () -> Unit, onError: () -> Unit) {
        val isUserOk = arrayOf(userFirstNameEt,
                            userLastnameEt,
                            birthCityEt,
                            birthDateEt,
                            addressStreetEt,
                            addressCityEt,
                            addressZipcodeEt ).noNullOrEmpty()

        val isDepartureOk = (getReasonFromRadio(radios.checkedRadioButtonId) != null &&
                            departureDateEt.editText?.text.isNullOrEmpty().not() &&
                            departureHourEt.editText?.text.isNullOrEmpty().not() )
        if ( isUserOk && isDepartureOk ) {
            onSuccess()
        } else { onError() }
    }

    private fun initObservers() {
        viewModel.formSave.observe(this.viewLifecycleOwner, Observer {
            if ( it is Resource.Success ) {
                wantSaveDatas.isChecked = it.data
            }
        })
        viewModel.user.observe(this.viewLifecycleOwner, Observer {
            if ( it is Resource.Success ) {
                it.data.run {
                    userFirstNameEt.quickSet( this.userName )
                    userLastnameEt.quickSet( this.userLastname )
                    birthCityEt.quickSet( this.birthCity )
                    birthDateEt.quickSet( this.birthDate )
                    addressStreetEt.quickSet( this.addressStreet )
                    addressCityEt.quickSet( this.addressCity )
                    addressZipcodeEt.quickSet( this.addressZipcode )
                }
            }
        })

        viewModel.goodwillEvent.observe(this.viewLifecycleOwner, Observer {
            if (it.getContentIfNotHandled() == true) {
                rootLayout?.showSnackbar(R.string.goodwill_alert_message,
                                         BaseTransientBottomBar.LENGTH_INDEFINITE,
                                         R.string.goodwill_action_button)
            }
        })
    }

    // region RadioTools

    private fun setRadioSelection(departure: Departure) {
        getRadioFromReason(departure.reason)?.isSelected = true
    }

    private fun getRadioFromReason(reason: Reason?) = when (reason) {
        Reason.TRAVAIL -> radioTravail
        Reason.COURSES -> radioCourses
        Reason.SANTE -> radioSante
        Reason.FAMILLE -> radioFamille
        Reason.SPORT -> radioSport
        Reason.JUDICIAIRE -> radioJudiciaire
        Reason.MISSIONS -> radioMissions
        else -> { null }
    }

    private fun getReasonFromRadio(radio: Int) : Reason? = when (radio) {
        R.id.radioTravail -> Reason.TRAVAIL
        R.id.radioCourses -> Reason.COURSES
        R.id.radioSante -> Reason.SANTE
        R.id.radioFamille -> Reason.FAMILLE
        R.id.radioSport -> Reason.SPORT
        R.id.radioJudiciaire -> Reason.JUDICIAIRE
        R.id.radioMissions -> Reason.MISSIONS
        else -> { null }
    }

    // endregion RadioTools

    override fun onPause() {
        super.onPause()
        putDatas()
    }

    private fun putDatas() {

        viewModel.putDatas(
            userFirstNameEt.quickToString(),
            userLastnameEt.quickToString(),
            birthCityEt.quickToString(),
            birthDateEt.quickToString(),
            addressStreetEt.quickToString(),
            addressCityEt.quickToString(),
            addressZipcodeEt.quickToString(),
            getReasonFromRadio(radios.checkedRadioButtonId),
            departureDateEt.quickToString(),
            departureHourEt.quickToString(),
            wantSaveDatas.isChecked
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    launchGenerationProcess()
                } else {
                    rootLayout?.showSnackbar(R.string.store_permission_required)
                }
                return
            }
        }
    }
}