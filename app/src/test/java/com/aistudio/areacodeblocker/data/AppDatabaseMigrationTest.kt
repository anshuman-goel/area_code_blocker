package com.aistudio.areacodeblocker.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.data.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(BlockerTestRunner::class)
@Config(sdk = [36])
class AppDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "migration-2-3-test"

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun `migration 2 to 3 preserves area codes logs and keywords`() {
        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(databaseName)
            .callback(
                object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `blocked_area_codes` (`areaCode` TEXT NOT NULL, `dateAdded` INTEGER NOT NULL, PRIMARY KEY(`areaCode`))"
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `blocked_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `phoneNumber` TEXT NOT NULL, `areaCode` TEXT NOT NULL, `messageBody` TEXT, `timestamp` INTEGER NOT NULL, `type` TEXT NOT NULL, `senderName` TEXT)"
                        )
                        db.execSQL(
                            "CREATE TABLE IF NOT EXISTS `blocked_keywords` (`keyword` TEXT NOT NULL, `dateAdded` INTEGER NOT NULL, PRIMARY KEY(`keyword`))"
                        )
                    }

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                }
            )
            .build()

        FrameworkSQLiteOpenHelperFactory().create(configuration).use { helper ->
            helper.writableDatabase.apply {
                execSQL("INSERT INTO blocked_area_codes(areaCode, dateAdded) VALUES ('512', 100)")
                execSQL(
                    "INSERT INTO blocked_logs(id, phoneNumber, areaCode, messageBody, timestamp, type, senderName) VALUES (1, '+15125550199', '512', NULL, 200, 'CALL', 'Unknown')"
                )
                execSQL("INSERT INTO blocked_keywords(keyword, dateAdded) VALUES ('lottery', 300)")
            }
        }

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()
        try {
            val sqliteDatabase = database.openHelper.writableDatabase

            sqliteDatabase.query("SELECT areaCode, regionLabel FROM blocked_area_codes").use { cursor ->
                cursor.moveToFirst()
                assertEquals("512", cursor.getString(0))
                assertNull(cursor.getString(1))
            }
            sqliteDatabase.query("SELECT phoneNumber FROM blocked_logs").use { cursor ->
                cursor.moveToFirst()
                assertEquals("+15125550199", cursor.getString(0))
            }
            sqliteDatabase.query("SELECT keyword FROM blocked_keywords").use { cursor ->
                cursor.moveToFirst()
                assertEquals("lottery", cursor.getString(0))
            }
        } finally {
            database.close()
        }
    }
}
