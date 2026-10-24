package com.aistudio.areacodeblocker.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.areacodeblocker.BlockerTestRunner
import com.example.data.AppDatabase
import com.example.data.dao.BlockedLogDao
import com.example.data.entity.BlockedLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(BlockerTestRunner::class)
@Config(sdk = [36])
class BlockedLogDaoPagingTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: BlockedLogDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.blockedLogDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `pages are ordered newest first and respect requested load size`() = runTest {
        repeat(50) { index -> dao.insert(log(id = index + 1, timestamp = index.toLong())) }
        val result = dao.getBlockedLogsPagingSource().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertEquals(10, result.data.size)
        assertEquals((50 downTo 41).toList(), result.data.map { it.id })
    }

    @Test
    fun `aggregate counts include call and all SMS variants`() = runTest {
        dao.insert(log(id = 1, type = "CALL"))
        dao.insert(log(id = 2, type = "SMS"))
        dao.insert(log(id = 3, type = "SMS (Silenced)"))

        assertEquals(3, dao.observeTotalCount().first())
        assertEquals(1, dao.observeCallCount().first())
        assertEquals(2, dao.observeSmsCount().first())
    }

    @Test
    fun `count flows update after insert and delete`() = runTest {
        val inserted = async { dao.observeTotalCount().first { it == 1 } }
        dao.insert(log(id = 1))
        assertEquals(1, inserted.await())

        val deleted = async { dao.observeTotalCount().first { it == 0 } }
        dao.deleteById(1)
        assertEquals(0, deleted.await())
    }

    @Test
    fun `paging source is invalidated after insert`() = runTest {
        val source = dao.getBlockedLogsPagingSource()
        source.load(PagingSource.LoadParams.Refresh(null, 10, false))
        assertFalse(source.invalid)
        dao.insert(log(id = 1))
        database.invalidationTracker.refreshAsync()
        kotlinx.coroutines.withTimeout(5_000) {
            while (!source.invalid) kotlinx.coroutines.delay(10)
        }
        assertTrue(source.invalid)
    }

    @Test
    fun `DAO log list query returns PagingSource rather than Flow List`() {
        val method = BlockedLogDao::class.java.getMethod("getBlockedLogsPagingSource")
        assertEquals(PagingSource::class.java, method.returnType)
        assertTrue(BlockedLogDao::class.java.methods.none {
            it.genericReturnType.typeName.contains("Flow<java.util.List<com.example.data.entity.BlockedLog")
        })
    }

    private fun log(
        id: Int,
        timestamp: Long = id.toLong(),
        type: String = "CALL",
    ) = BlockedLog(
        id = id,
        phoneNumber = "+1512555${id.toString().padStart(4, '0')}",
        areaCode = "512",
        messageBody = null,
        timestamp = timestamp,
        type = type,
        senderName = null,
    )
}
