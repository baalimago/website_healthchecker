package com.imago.websitehealthcheck

import android.content.ContentValues
import android.util.Log
import com.imago.websitehealthcheck.data.CheckResult
import com.imago.websitehealthcheck.data.State
import com.imago.websitehealthcheck.data.Website
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.UnknownHostException
import java.util.Date

class WebsiteChecker {

    companion object {
        fun check(website: Website): CheckResult {
            val URL = website.url
            val client = OkHttpClient()
            val request = Request.Builder().url(URL).build()

            return try {
                val response = request.let { client.newCall(it).execute() }
                val statusCode = response.code
                var healthState = State.HEALTHY
                Log.e(ContentValues.TAG, "Status code for: $URL is: $statusCode")
                var respBody = ""
                if (statusCode != 200) {
                    respBody = response.body?.string().toString()
                    healthState = State.UNHEALTHY
                }
                response.close()

                CheckResult(
                    checkDate = Date(),
                    status = statusCode,
                    responseText = respBody,
                    websiteId = website.id,
                    healthState = healthState
                )
            } catch (e: UnknownHostException) {
                CheckResult(
                    checkDate = Date(),
                    status = 404,
                    responseText = "Unknown host",
                    websiteId = website.id
                )
            }

        }
    }
}