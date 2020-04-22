package fr.useless.depdero.ui.main

import android.content.Intent
import android.content.Intent.createChooser
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import fr.useless.depdero.R
import fr.useless.depdero.core.tool.getShowPdfFileIntent
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private lateinit var viewModel : MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initViews()
        initListeners()
    }

    private fun initViews() {
        lastBtn.isEnabled =  viewModel.hasFile()
    }

    private fun initListeners() {
        formBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_formFragment)
        }

        lastBtn.setOnClickListener {
            startActivity(createChooser( getPdfIntent() , getString(R.string.create_pdf_chooser)))
        }

        settingsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }
    }

    private fun getPdfIntent(): Intent? {
        return context?.getShowPdfFileIntent( viewModel.lastFilePath )
    }

}
