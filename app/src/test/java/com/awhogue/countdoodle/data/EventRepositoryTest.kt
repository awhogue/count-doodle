package com.awhogue.countdoodle.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: EventDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.eventDao()
    }

    @After fun tearDown() = db.close()

    @Test fun `upsert insert returns new id and notifies onChanged`() = runTest {
        var changed = 0
        val repo = EventRepository(dao) { changed++ }
        val id = repo.upsert(Event(name = "Trip", dateEpochMillis = 1000L))
        assertThat(id).isGreaterThan(0L)
        assertThat(changed).isEqualTo(1)
    }

    @Test fun `upsert update modifies existing row`() = runTest {
        val repo = EventRepository(dao)
        val id = repo.upsert(Event(name = "Trip", dateEpochMillis = 1000L))
        repo.upsert(Event(id = id, name = "Trip Renamed", dateEpochMillis = 2000L))
        val fetched = repo.getById(id)
        assertThat(fetched?.name).isEqualTo("Trip Renamed")
        assertThat(fetched?.dateEpochMillis).isEqualTo(2000L)
    }

    @Test fun `delete removes and notifies`() = runTest {
        var changed = 0
        val repo = EventRepository(dao) { changed++ }
        val id = repo.upsert(Event(name = "Bye", dateEpochMillis = 100L))
        changed = 0
        repo.delete(id)
        assertThat(repo.getById(id)).isNull()
        assertThat(changed).isEqualTo(1)
    }

    @Test fun `getUpcoming respects limit and ordering`() = runTest {
        val repo = EventRepository(dao)
        repo.upsert(Event(name = "A", dateEpochMillis = 100L))
        repo.upsert(Event(name = "B", dateEpochMillis = 50L))
        repo.upsert(Event(name = "C", dateEpochMillis = 200L))
        val upcoming = repo.getUpcoming(now = 0L, limit = 2)
        assertThat(upcoming.map { it.name }).containsExactly("B", "A").inOrder()
    }
}
