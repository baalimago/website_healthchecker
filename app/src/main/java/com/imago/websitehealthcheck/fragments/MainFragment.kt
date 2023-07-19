package com.imago.websitehealthcheck.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.imago.websitehealthcheck.MainActivity
import com.imago.websitehealthcheck.R
import com.imago.websitehealthcheck.RVAdapter
import com.imago.websitehealthcheck.data.CheckResult
import com.imago.websitehealthcheck.data.HealthCheckerViewModel
import com.imago.websitehealthcheck.data.HealthCheckerViewModelFactory
import com.imago.websitehealthcheck.data.Website
import com.imago.websitehealthcheck.data.WebsiteHealthcheckerDatabase
import com.imago.websitehealthcheck.data.WebsiteWithLastResult
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

// Request code for creating a PDF document.
const val CREATE_FILE = 1

class MainFragment : Fragment() {

    private lateinit var frag: Fragment
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var contentResolver: ContentResolver
    private var result: List<CheckResult> = ArrayList()

    private val database: WebsiteHealthcheckerDatabase by lazy {
        WebsiteHealthcheckerDatabase.getDatabase(requireContext())
    }
    private val viewModel: HealthCheckerViewModel by viewModels {
        HealthCheckerViewModelFactory(database.websiteDao(), database.checkResultDao())
    }

    private lateinit var workManager: WorkManager

    private val adapter = RVAdapter(object : RVAdapter.OnClickListener {
        override fun onClick(position: Int, model: LiveData<WebsiteWithLastResult?>) {
            model.value?.let { viewModel.deleteWebsite(it.website) }
        }
    }, object : RVAdapter.OnClickListener {
        override fun onClick(position: Int, model: LiveData<WebsiteWithLastResult?>) {
            val main: MainActivity = activity as MainActivity
            main.navigateToWebsiteModifier(model.value?.website)
        }
    }, object : RVAdapter.OnClickListener {
        override fun onClick(position: Int, model: LiveData<WebsiteWithLastResult?>) {
            model.value?.let {
                viewModel.getCheckResultsFor(it.website.id).observeOnce(frag) { checkResults ->
                    run {
                        result = checkResults
                        createFile(it.website)
                    }
                }
            }
        }
    })

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }
        })
    }

    private fun createFile(ws: Website) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "${ws.name}_check-results.csv")
        }
        resultLauncher.launch(intent)
    }

    private fun parseCsvFromLocalVar(res: List<CheckResult>): ByteArray {
        var ret = "Date,Status,ResponseText\n"
        res.forEach {
            ret +=  "${it.checkDate},${it.status},${it.responseText}\n"
        }
        return ret.toByteArray()
    }

    private fun writeToUri(uri: Uri) {
        var fileDescriptor: ParcelFileDescriptor? = null
        try {
            fileDescriptor = contentResolver.openFileDescriptor(uri, "w")
            if (fileDescriptor != null) {
                val outputStream = FileOutputStream(fileDescriptor.fileDescriptor)
                val toWrite = parseCsvFromLocalVar(result)
                outputStream.write(toWrite)
                outputStream.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileDescriptor?.close()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.content_main, container, false)
        workManager = WorkManager.getInstance(requireContext())
        frag = this
        contentResolver = requireContext().contentResolver
        val rv = view.findViewById<RecyclerView>(R.id.recycler_view)
        rv?.setHasFixedSize(true)
        val llm = LinearLayoutManager(context)
        rv.layoutManager = llm
        rv.adapter = adapter

        viewModel.getAll().observeForever {
            val ldList = ArrayList<LiveData<WebsiteWithLastResult?>>()
            it.forEach { ws ->
                run {
                    ldList.add(viewModel.get(ws))
                }
            }
            adapter.setWebsites(ldList)
            adapter.notifyDataSetChanged()
        }

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val uri = result.data!!.data
                    uri?.let {
                        writeToUri(uri)
                    }
                }
            }

        return view
    }
}