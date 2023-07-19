package com.imago.websitehealthcheck.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.util.Date




@Entity(tableName = "website")
data class Website(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String,
    var url: String,
    var intervalMs: Long = 10000)