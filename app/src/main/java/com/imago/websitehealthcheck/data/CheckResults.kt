package com.imago.websitehealthcheck.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "check_results")
data class CheckResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val websiteId: Long, // Foreign key to link the CheckResult with the Website
    val checkDate: Date,
    val healthState: State = State.UNKNOWN,
    val status: Int,
    val responseText: String = "" // Only shown on non failing request
)