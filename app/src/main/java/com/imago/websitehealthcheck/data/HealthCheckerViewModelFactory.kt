package com.imago.websitehealthcheck.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class HealthCheckerViewModelFactory(
    private val websiteDao: WebsiteDao,
    private val checkResultsDao: CheckResultsDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthCheckerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthCheckerViewModel(websiteDao, checkResultsDao) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass::class.java)
    }
}