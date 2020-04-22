package fr.useless.depdero.ui.settings

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import fr.useless.depdero.R
import fr.useless.depdero.core.tool.rateApp
import fr.useless.depdero.core.tool.share
import fr.useless.depdero.core.tool.showSnackbar

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var viewModel : SettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        initListeners()
    }

    private fun initListeners() {

        findPreference<Preference>("cleanDatas")?.setOnPreferenceClickListener {
            viewModel.cleanDatas()
            view?.showSnackbar(R.string.clean_datas_success)
            true
        }

        findPreference<Preference>("rate")?.setOnPreferenceClickListener {
            this.context?.rateApp()
            true
        }

        findPreference<Preference>("share")?.setOnPreferenceClickListener {
            this.context?.share(
                    "${getString(R.string.share_description)} ${getString(R.string.github_url)}",
                    getString(R.string.app_name)
                )
            true
        }

    }

}