package com.wristcall.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wristcall_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateSeedData(database.taskDao())
                    }
                }
            }
        }

        private suspend fun populateSeedData(taskDao: TaskDao) {
            taskDao.insertTask(
                TaskEntity(
                    targetNumber = "+1 (555) 019-9000",
                    taskPrompt = "Call Joe's Diner for table for 4 at 8 PM",
                    status = "SUCCESS",
                    summary = "✅ Table Reserved for 4 @ 8:00 PM under name Alex",
                    transcript = """
                        [AGENT]: Hello! I'm calling on behalf of WristCall AI to confirm a reservation.
                        [RECIPIENT]: Hi there! Sure, what date and time are you looking for?
                        [AGENT]: Table for 4 people tonight at 8:00 PM under the name Alex.
                        [RECIPIENT]: Perfect, we have a table available at 8:00 PM for 4. Reserved!
                        [AGENT]: Thank you so much! Have a great evening.
                    """.trimIndent(),
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )

            taskDao.insertTask(
                TaskEntity(
                    targetNumber = "+1 (555) 019-9001",
                    taskPrompt = "Call Hardware Store & check 1/2 inch pipes in stock",
                    status = "SUCCESS",
                    summary = "✅ 12 units of 1/2 inch copper pipes in stock @ \$4.50/ft",
                    transcript = """
                        [AGENT]: Hi! Calling to check stock availability for 1/2 inch copper pipes.
                        [RECIPIENT]: Hey! Yes, we have 12 units in stock priced at \$4.50 per foot.
                        [AGENT]: Great, thank you!
                    """.trimIndent(),
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }
    }
}
