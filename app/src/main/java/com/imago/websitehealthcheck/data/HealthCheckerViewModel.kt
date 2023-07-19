package com.imago.websitehealthcheck.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.Observer
import kotlinx.coroutines.launch

class HealthCheckerViewModel(
    private val websiteDao: WebsiteDao,
    private val checkResultsDao: CheckResultsDao
) : ViewModel() {

    fun insertWebsite(website: Website) {
        viewModelScope.launch {
            websiteDao.insert(website)
        }
    }

    fun updateWebsite(website: Website) {
        viewModelScope.launch {
            websiteDao.update(website)
        }
    }

    fun deleteWebsite(website: Website) {
        viewModelScope.launch {
            websiteDao.delete(website)
        }
    }

    fun getAll(): LiveData<List<Website>>{
        return websiteDao.getAllLd()
    }

    fun get(website: Website): LiveData<WebsiteWithLastResult?> {
        return websiteDao.getWebsiteWithLastResult(website.id)
    }

    fun addCheckResult(checkResult: CheckResult) {
        viewModelScope.launch {
            checkResultsDao.insert(checkResult)
        }
    }

    fun getCheckResultsFor(websiteId: Long): LiveData<List<CheckResult>> {
        return checkResultsDao.getAllFor(websiteId)
    }

    fun deleteCheckResultsFor(website: Website) {
        viewModelScope.launch {
            checkResultsDao.deleteAllFor(website.id)
        }
    }
}
