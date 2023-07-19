package com.imago.websitehealthcheck.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.imago.websitehealthcheck.data.Website
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(website: Website): Long

    @Update
    suspend fun update(website: Website)

    @Delete
    suspend fun delete(website: Website)

    @Query("SELECT * FROM website WHERE id = :id")
    fun get(id: Long): LiveData<Website>

    @Query("SELECT * FROM website WHERE id = :id")
    fun getWebsiteWithLastResult(id: Long): LiveData<WebsiteWithLastResult?>

    @Query("SELECT * FROM website WHERE id = :id")
    fun getInstance(id: Long): Website

    @Query("SELECT * FROM website")
    fun getAll(): Flow<List<Website>>

    @Query("SELECT * FROM website")
    fun getAllLd(): LiveData<List<Website>>

}