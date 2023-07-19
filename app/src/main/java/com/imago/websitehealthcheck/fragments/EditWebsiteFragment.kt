package com.imago.websitehealthcheck.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.imago.websitehealthcheck.MainActivity
import com.imago.websitehealthcheck.R
import com.imago.websitehealthcheck.data.Website
import com.imago.websitehealthcheck.data.WebsiteHealthcheckerDatabase
import com.imago.websitehealthcheck.data.HealthCheckerViewModel
import com.imago.websitehealthcheck.data.HealthCheckerViewModelFactory
import java.util.concurrent.atomic.AtomicReference

class EditWebsiteFragment(private val website: Website): Fragment() {
    private val database: WebsiteHealthcheckerDatabase by lazy {
        WebsiteHealthcheckerDatabase.getDatabase(requireContext())
    }
    private val viewModel: HealthCheckerViewModel by viewModels {
        HealthCheckerViewModelFactory(database.websiteDao(), database.checkResultDao())
    }

        // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modify_website, container, false)
        val setWebsiteName: EditText = view.findViewById(R.id.set_website_name)
        setWebsiteName.setText(website.name)
        val setWebsiteURL: EditText = view.findViewById(R.id.set_website_url)
        setWebsiteURL.setText(website.url.substringAfter("://"))
        val protocol = AtomicReference("https")
        val radioGroup: RadioGroup = view.findViewById(R.id.protocol_radio_group)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedButton: RadioButton = view.findViewById(checkedId)
            protocol.set(checkedButton.text as String?)
        }

        val btnSave: Button = view.findViewById(R.id.btn_save)
        val btnCancel: Button = view.findViewById(R.id.btn_cancel)

        btnSave.setOnClickListener {
            val newWebsiteName = setWebsiteName.text.toString()
            val newWebsiteUrl = setWebsiteURL.text.toString()
            val urlWithProtocol = "${protocol.get()}://${newWebsiteUrl}"
            website.name = newWebsiteName
            website.url = urlWithProtocol
            viewModel.updateWebsite(website)
            navigateToMain()
        }

        btnCancel.setOnClickListener {
            navigateToMain()
        }

        return view
    }

    private fun navigateToMain() {
        val fragmentManager = parentFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val mainFragment = MainFragment()
        transaction.replace(R.id.nav_host_fragment, mainFragment)
        val main: MainActivity = activity as MainActivity
        main.convertToAddNewButton()
        transaction.commit()
    }

}