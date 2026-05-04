package com.awhogue.countdoodle.ui.edit

import app.cash.turbine.test
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.awhogue.countdoodle.data.AppDatabase
import com.awhogue.countdoodle.data.EventRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class EventEditViewModelTest {

    @get:Rule val rule = InstantTaskExecutorRule()

    private lateinit var db: AppDatabase
    private lateinit var repo: EventRepository

    private val zone = ZoneId.of("America/Los_Angeles")

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repo = EventRepository(db.eventDao())
    }

    @After fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test fun `form is invalid when name is blank`() = runTest {
        val vm = EventEditViewModel(repo, zone)
        assertThat(vm.form.value.isValid).isFalse()
        vm.setName("Hello")
        assertThat(vm.form.value.isValid).isTrue()
        vm.setName("   ")
        assertThat(vm.form.value.isValid).isFalse()
    }

    @Test fun `save persists event with correct epoch from date and time`() = runTest {
        val vm = EventEditViewModel(repo, zone)
        vm.setName("Birthday")
        vm.setDate(LocalDate.of(2026, 7, 4))
        vm.setHasTime(true)
        vm.setTime(LocalTime.of(18, 30))

        var done = false
        vm.save { done = true }

        assertThat(done).isTrue()
        repo.observeAll().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            val saved = list[0]
            val expectedMillis = java.time.ZonedDateTime
                .of(LocalDate.of(2026, 7, 4), LocalTime.of(18, 30), zone)
                .toInstant().toEpochMilli()
            assertThat(saved.dateEpochMillis).isEqualTo(expectedMillis)
            assertThat(saved.hasTime).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `save with hasTime false uses midnight`() = runTest {
        val vm = EventEditViewModel(repo, zone)
        vm.setName("Trip")
        vm.setDate(LocalDate.of(2026, 7, 4))
        vm.setHasTime(false)
        vm.setTime(LocalTime.of(18, 30)) // should be ignored

        vm.save { }
        val list = repo.getUpcoming(0L, 10)
        val expectedMillis = java.time.ZonedDateTime
            .of(LocalDate.of(2026, 7, 4), LocalTime.MIDNIGHT, zone)
            .toInstant().toEpochMilli()
        assertThat(list[0].dateEpochMillis).isEqualTo(expectedMillis)
    }

    @Test fun `save does not persist when invalid`() = runTest {
        val vm = EventEditViewModel(repo, zone)
        var done = false
        vm.save { done = true }
        assertThat(done).isFalse()
        assertThat(repo.getUpcoming(0L, 10)).isEmpty()
    }

    @Test fun `loadIfExisting populates form from db`() = runTest {
        val id = repo.upsert(
            com.awhogue.countdoodle.data.Event(
                name = "Existing",
                dateEpochMillis = java.time.ZonedDateTime.of(
                    LocalDate.of(2026, 9, 1), LocalTime.of(10, 0), zone
                ).toInstant().toEpochMilli(),
                hasTime = true,
            )
        )
        val vm = EventEditViewModel(repo, zone)
        vm.loadIfExisting(id)
        val s = vm.form.value
        assertThat(s.id).isEqualTo(id)
        assertThat(s.name).isEqualTo("Existing")
        assertThat(s.date).isEqualTo(LocalDate.of(2026, 9, 1))
        assertThat(s.hasTime).isTrue()
        assertThat(s.time).isEqualTo(LocalTime.of(10, 0))
    }

    @Test fun `delete removes existing event`() = runTest {
        val id = repo.upsert(
            com.awhogue.countdoodle.data.Event(name = "X", dateEpochMillis = 1000L)
        )
        val vm = EventEditViewModel(repo, zone)
        vm.loadIfExisting(id)
        var done = false
        vm.delete { done = true }
        assertThat(done).isTrue()
        assertThat(repo.getById(id)).isNull()
    }
}
