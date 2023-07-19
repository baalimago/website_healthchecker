package com.imago.websitehealthcheck

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton
import com.imago.websitehealthcheck.data.Website
import com.imago.websitehealthcheck.fragments.AddWebsiteFragment
import com.imago.websitehealthcheck.fragments.EditWebsiteFragment
import com.imago.websitehealthcheck.fragments.MainFragment
import com.imago.websitehealthcheck.services.CheckingService


class MainActivity : AppCompatActivity() {
    private lateinit var notificationManager: NotificationManager

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e(
            ContentValues.TAG,
            "onRequestPermissionsResult: $requestCode, $permissions, $grantResults"
        )
    }

    @RequiresApi(34)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e(ContentValues.TAG, "==Starting application==")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.areNotificationsEnabled()) {
            Log.e(ContentValues.TAG, "Notifications aren't enabled")
            requestPermissions(arrayOf(POST_NOTIFICATIONS), 2)
        }
        setupNotificationChannel()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setContentView(R.layout.activity_main)
        convertToAddNewButton()
        val intent = Intent(applicationContext, CheckingService::class.java)
        applicationContext.startForegroundService(intent)
    }

    fun navigateToWebsiteModifier(website: Website?) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        var fragment: Fragment = AddWebsiteFragment()
        if(website != null) {
            fragment = EditWebsiteFragment(website)
        }
        transaction.replace(R.id.nav_host_fragment, fragment)
        convertButtonToBack()
        transaction.commit()
    }

    private fun navigateToMain() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val mainFragment = MainFragment()
        transaction.replace(R.id.nav_host_fragment, mainFragment)
        convertToAddNewButton()
        transaction.commit()
    }

    fun convertToAddNewButton() {
        val toolbarBtn = findViewById<MaterialButton>(R.id.toolbar_btn)
        toolbarBtn.text = "Add Website"
        val icon = ContextCompat.getDrawable(this, R.drawable.baseline_add_task_24)
        toolbarBtn.icon = icon
        toolbarBtn.setOnClickListener {
            navigateToWebsiteModifier(null)
        }
    }

    private fun convertButtonToBack() {
        val toolbarBtn = findViewById<MaterialButton>(R.id.toolbar_btn)
        toolbarBtn.text = "Back"
        val icon = ContextCompat.getDrawable(this, com.google.android.material.R.drawable.material_ic_keyboard_arrow_left_black_24dp)
        toolbarBtn.icon = icon
        toolbarBtn.setOnClickListener {
            navigateToMain()
        }
    }

    private fun setupNotificationChannel() {
        val persistentChannel =
            NotificationChannel(applicationContext.getString(R.string.notification_channel_id_persistent),
                "lorentz.app foreground notification",
                NotificationManager.IMPORTANCE_HIGH).apply {description = "Foreground notification showing that there's healthchecks running"}
        val transientChannel =
            NotificationChannel(applicationContext.getString(R.string.notification_channel_id_transient),
                "lorentz.app state change notification",
                NotificationManager.IMPORTANCE_DEFAULT).apply {description = "Notifies about changes in the website health"}
        notificationManager.createNotificationChannel(persistentChannel)
        notificationManager.createNotificationChannel(transientChannel)
    }
}