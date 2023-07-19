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
import com.imago.websitehealthcheck.data.HealthCheckerViewModel
import com.imago.websitehealthcheck.data.Website
import com.imago.websitehealthcheck.data.WebsiteHealthcheckerDatabase
import com.imago.websitehealthcheck.data.HealthCheckerViewModelFactory
import java.util.concurrent.atomic.AtomicReference

class AddWebsiteFragment : Fragment() {
    private val database: WebsiteHealthcheckerDatabase by lazy {
        WebsiteHealthcheckerDatabase.getDatabase(requireContext())
    }
    private val viewModel: HealthCheckerViewModel by viewModels {
        HealthCheckerViewModelFactory(database.websiteDao(), database.checkResultDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modify_website, container, false)
        val protocol = AtomicReference("https")
        val radioGroup: RadioGroup = view.findViewById(R.id.protocol_radio_group)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedButton: RadioButton = view.findViewById(checkedId)
            protocol.set(checkedButton.text as String?)
        }

        val btnSave: Button = view.findViewById(R.id.btn_save)
        val btnCancel: Button = view.findViewById(R.id.btn_cancel)

        btnSave.setOnClickListener {
            val editText = view.findViewById<EditText>(R.id.set_website_name)
            val newWebsiteName = editText.text.toString()
            val newWebsiteUrl = view.findViewById<EditText>(R.id.set_website_url).text.toString()
            val urlWithProtocol = "${protocol.get()}://${newWebsiteUrl}"
            viewModel.insertWebsite(Website(name = newWebsiteName, url = urlWithProtocol))
            navigateToMain()
        }

        btnCancel.setOnClickListener {
            navigateToMain()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
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