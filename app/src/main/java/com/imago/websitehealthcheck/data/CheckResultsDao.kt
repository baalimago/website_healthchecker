package com.imago.websitehealthcheck.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CheckResultsDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(checkResult: CheckResult)

    @Query("DELETE FROM check_results WHERE websiteId = :websiteId")
    fun deleteAllFor(websiteId: Long)

    @Query("SELECT * FROM check_results WHERE websiteId = :websiteId")
    fun getAllFor(websiteId: Long): LiveData<List<CheckResult>>
}