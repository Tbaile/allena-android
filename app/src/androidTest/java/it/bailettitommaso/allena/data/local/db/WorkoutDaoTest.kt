package it.bailettitommaso.allena.data.local.db

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var database: AllenaDatabase
    private lateinit var planDao: WorkoutPlanDao
    private lateinit var sessionDao: WorkoutSessionDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AllenaDatabase::class.java).build()
        planDao = database.workoutPlanDao()
        sessionDao = database.workoutSessionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private val plan = WorkoutPlanEntity(id = 3, name = "Full Body A", description = "Demo.", isActive = true)

    private fun item(id: Long, position: Int) = WorkoutPlanItemEntity(
        id = id,
        planId = 3,
        exerciseId = 5,
        position = position,
        sets = 4,
        reps = 8,
        durationSeconds = null,
        restSeconds = 120,
        targetWeight = 60.0,
        notes = null,
    )

    private fun session(startedAtMillis: Long, isPending: Boolean, remoteId: Long? = null) = WorkoutSessionEntity(
        remoteId = remoteId,
        planId = 3,
        planName = "Full Body A",
        startedAtMillis = startedAtMillis,
        completedAtMillis = startedAtMillis + 1_000,
        notes = null,
        setCount = 0,
        totalVolume = 0.0,
        isPending = isPending,
    )

    @Test
    fun planItemsComeBackInPrescribedOrder() = runTest {
        planDao.replacePlan(plan, listOf(item(id = 12, position = 2), item(id = 11, position = 1)))

        assertEquals(listOf(1, 2), planDao.getItems(3).map { it.position })
        assertEquals(60.0, planDao.getItem(11)?.targetWeight!!, 0.0)
    }

    @Test
    fun replacingAPlanDropsItemsRemovedUpstream() = runTest {
        planDao.replacePlan(plan, listOf(item(id = 11, position = 1), item(id = 12, position = 2)))

        planDao.replacePlan(plan, listOf(item(id = 11, position = 1)))

        assertEquals(listOf(11L), planDao.getItems(3).map { it.id })
        assertNull(planDao.getItem(12))
    }

    @Test
    fun sessionsAreOrderedNewestFirst() = runTest {
        sessionDao.insertSession(session(startedAtMillis = 1_000, isPending = false, remoteId = 1))
        sessionDao.insertSession(session(startedAtMillis = 5_000, isPending = false, remoteId = 2))

        assertEquals(listOf(5_000L, 1_000L), sessionDao.sessions().first().map { it.startedAtMillis })
    }

    @Test
    fun refreshingHistoryKeepsPendingSessions() = runTest {
        val pendingId = sessionDao.insertSession(session(startedAtMillis = 9_000, isPending = true))
        sessionDao.insertSession(session(startedAtMillis = 1_000, isPending = false, remoteId = 1))

        sessionDao.replaceSynced(listOf(session(startedAtMillis = 2_000, isPending = false, remoteId = 2)))

        val stored = sessionDao.sessions().first()
        assertEquals(2, stored.size)
        assertEquals(pendingId, stored.first { it.isPending }.localId)
        assertEquals(listOf(2L), stored.filter { !it.isPending }.map { it.remoteId })
    }

    @Test
    fun finishingASessionRecordsVolumeAndSyncClearsPending() = runTest {
        val localId = sessionDao.insertSession(session(startedAtMillis = 1_000, isPending = true))
        sessionDao.insertSetLog(
            WorkoutSetLogEntity(sessionLocalId = localId, planItemId = 11, setNumber = 1, reps = 8, weight = 60.0, durationSeconds = null),
        )

        sessionDao.markFinished(localId, completedAtMillis = 4_000, notes = "Done.", setCount = 1, totalVolume = 480.0)
        assertEquals(1, sessionDao.getSetLogs(localId).size)
        assertTrue(sessionDao.getPending().single().localId == localId)

        sessionDao.markSynced(localId, remoteId = 77)

        val synced = sessionDao.getSession(localId)!!
        assertFalse(synced.isPending)
        assertEquals(77L, synced.remoteId)
        assertEquals(480.0, synced.totalVolume, 0.0)
        assertTrue(sessionDao.getPending().isEmpty())
    }
}
