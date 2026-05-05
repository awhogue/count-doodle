package org.secondthought.countdoodle.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventDaoTest {

    @get:Rule val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var dao: EventDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.eventDao()
    }

    @After fun tearDown() = db.close()

    private fun event(name: String, dateMillis: Long, hasTime: Boolean = false) =
        Event(name = name, dateEpochMillis = dateMillis, hasTime = hasTime)

    @Test fun `insert and observeAll emits inserted event`() = runTest {
        val id = dao.insert(event("Birthday", 1_000_000L))
        dao.observeAll().test {
            val items = awaitItem()
            assertThat(items).hasSize(1)
            assertThat(items[0].id).isEqualTo(id)
            assertThat(items[0].name).isEqualTo("Birthday")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `observeAll orders by date ascending`() = runTest {
        dao.insert(event("Late", 5_000L))
        dao.insert(event("Early", 1_000L))
        dao.insert(event("Mid", 3_000L))
        dao.observeAll().test {
            val items = awaitItem()
            assertThat(items.map { it.name }).containsExactly("Early", "Mid", "Late").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `observeUpcoming returns next N future events sorted by date, excludes past`() = runTest {
        val now = 10_000L
        dao.insert(event("Past1", 1_000L))
        dao.insert(event("Past2", 5_000L))
        dao.insert(event("Future1", 20_000L))
        dao.insert(event("Future2", 15_000L))
        dao.insert(event("Future3", 30_000L))
        dao.insert(event("Future4", 25_000L))

        dao.observeUpcoming(now, limit = 3).test {
            val items = awaitItem()
            assertThat(items.map { it.name }).containsExactly("Future2", "Future1", "Future4").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `update replaces fields`() = runTest {
        val id = dao.insert(event("Old", 100L))
        dao.update(Event(id = id, name = "New", dateEpochMillis = 200L, hasTime = true))
        val fetched = dao.getById(id)
        assertThat(fetched?.name).isEqualTo("New")
        assertThat(fetched?.hasTime).isTrue()
    }

    @Test fun `delete removes event`() = runTest {
        val id = dao.insert(event("Bye", 100L))
        dao.deleteById(id)
        assertThat(dao.getById(id)).isNull()
    }
}
