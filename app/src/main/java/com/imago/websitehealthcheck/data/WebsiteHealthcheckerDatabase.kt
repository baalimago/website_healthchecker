package com.imago.websitehealthcheck.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.imago.websitehealthcheck.fragments.MainFragment

@Database(entities = [Website::class, CheckResult::class],
    version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class WebsiteHealthcheckerDatabase : RoomDatabase() {

    abstract fun websiteDao(): WebsiteDao
    abstract fun checkResultDao(): CheckResultsDao

    companion object {
        @Volatile
        private var INSTANCE: WebsiteHealthcheckerDatabase? = null

        fun getDatabase(context: Context): WebsiteHealthcheckerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    WebsiteHealthcheckerDatabase::class.java,
                    "website_healthchecker_db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}