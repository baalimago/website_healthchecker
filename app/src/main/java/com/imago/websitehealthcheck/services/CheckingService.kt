package com.imago.websitehealthcheck.services

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.imago.websitehealthcheck.R
import com.imago.websitehealthcheck.WebsiteChecker
import com.imago.websitehealthcheck.data.CheckResult
import com.imago.websitehealthcheck.data.HealthCheckerViewModel
import com.imago.websitehealthcheck.data.Website
import com.imago.websitehealthcheck.data.WebsiteHealthcheckerDatabase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class CheckingService : LifecycleService() {
    private val routineMap = ConcurrentHashMap<Long, Job>()
    private val previousHealthMap = mutableMapOf<Long, CheckResult>()
    private var notificationId = 10;
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private lateinit var INSTANCE: CheckingService
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val database: WebsiteHealthcheckerDatabase by lazy {
        WebsiteHealthcheckerDatabase.getDatabase(this)
    }

    private lateinit var viewModel: HealthCheckerViewModel
    override fun onCreate() {
        super.onCreate()
        viewModel = HealthCheckerViewModel(database.websiteDao(), database.checkResultDao())

        viewModel.getAll().observeForever {
            val a = HashSet<Long>()
            it.forEach { website ->
                a.add(website.id)
                if (!routineMap.containsKey(website.id)) {
                    startRoutine(database.websiteDao().get(website.id))
                }
            }

            routineMap.forEach { (id, _) ->
                if (!a.contains(id)) {
                    stopRoutine(id)
                }
            }
        }

        INSTANCE = this
    }

    private fun hasStateChanged(newResult: CheckResult): Boolean {
        val lastResult = previousHealthMap[newResult.websiteId]
        if (lastResult != null) {
            if (lastResult.healthState != newResult.healthState) {
                return true
            }
        } else {
            return true
        }
        return false
    }

    private fun notifyStateChange(
        website: Website,
        newResult: CheckResult,
        previousResult: CheckResult?
    ) {
        val shortDescr: String
        var fullDescr: String
        if (previousResult == null) {
            shortDescr = "Initial healtcheck"
            fullDescr =
               "Latest state: ${newResult.healthState}\n" +
               "Status code: ${newResult.status}"
            if (newResult.responseText != "") {
                fullDescr += "\nResponse text: ${newResult.status}"
            }
        } else {
            shortDescr = "State has gone from ${previousResult.healthState} -> ${newResult.healthState}"
            fullDescr =
                "Old state: ${previousResult.healthState}\n"+
                "Latest state: ${newResult.healthState}\n" +
                "Status code: ${newResult.status}"
            if (newResult.responseText != "") {
                fullDescr += "\nResponse text: ${newResult.status}"
            }
        }

        val builder =
            NotificationCompat.Builder(this, applicationContext.getString(R.string.notification_channel_id_transient))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("${website.name} is now ${newResult.healthState}")
                .setContentText(shortDescr)
                .setStyle(NotificationCompat.BigTextStyle().bigText(fullDescr))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notify(notificationId++, builder.build())
        }

    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(value: T) {
                observer.onChanged(value)
                removeObserver(this)
            }
        })
    }

    private fun startRoutine(websiteLd: LiveData<Website>) {
        val atomicWebsite = AtomicReference<Website?>(websiteLd.value)
        websiteLd.observe(INSTANCE) {
            atomicWebsite.set(it)
        }
        val checkWebsiteJob = serviceScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            // Jitter so that not all requests occur at the same time
            delay((Math.random() * 5000).toLong())
            while (true) {
                val websiteCopy = atomicWebsite.get()
                websiteCopy?.let {
                    val checkResult = WebsiteChecker.check(websiteCopy)
                    if (hasStateChanged(checkResult)) {
                        notifyStateChange(websiteCopy,checkResult,previousHealthMap[websiteCopy.id])
                    }
                    previousHealthMap[websiteCopy.id] = checkResult
                    viewModel.addCheckResult(checkResult)
                    delay(websiteCopy.intervalMs)
                } ?: run {
                    // Delay short time to await initial db query, which will update the atomic reference
                    delay(100)
                }

            }
        }
        websiteLd.observeOnce(this) { website ->
            run {
                routineMap[website.id] = checkWebsiteJob
            }
        }
    }

    private fun stopRoutine(websiteId: Long) {
        val job = routineMap.remove(websiteId)
        job?.cancel("Routine has been stopped, probably removed")
    }

    private fun stopAll() {
        routineMap.forEach { (i, _) ->
            stopRoutine(i)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAll()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val title = "Website healthchecker running"
        val notification = NotificationCompat.Builder(applicationContext, applicationContext
            .getString(R.string.notification_channel_id_persistent))
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("Currently running background healthchecks...")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
        return START_STICKY
    }
}


