package it.bailettitommaso.allena.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import it.bailettitommaso.allena.di.MIGRATION_1_2
import it.bailettitommaso.allena.di.MIGRATION_2_3
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Opening a version 2 database with the version 3 schema makes Room run [MIGRATION_2_3] and then
 * validate the tables it produced against the ones it expects, so a mistake in the hand-written
 * DDL fails here rather than on a user's phone after an update.
 */
@RunWith(AndroidJUnit4::class)
class WorkoutMigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        context.deleteDatabase(DB_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(DB_NAME)
    }

    @Test
    fun migratesFromVersion2AndKeepsCachedExercises() = runTest {
        seedVersion2Database()

        val database = Room.databaseBuilder(context, AllenaDatabase::class.java, DB_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

        try {
            // Reading through the new DAOs forces Room to open, migrate and validate the schema.
            database.workoutPlanDao().replacePlan(
                WorkoutPlanEntity(id = 3, name = "Full Body A", description = null, isActive = true),
                listOf(
                    WorkoutPlanItemEntity(
                        id = 11, planId = 3, exerciseId = 5, position = 1, sets = 4, reps = 8,
                        durationSeconds = null, restSeconds = 120, targetWeight = 60.0, notes = null,
                    ),
                ),
            )
            val localId = database.workoutSessionDao().insertSession(
                WorkoutSessionEntity(
                    remoteId = null, planId = 3, planName = "Full Body A", startedAtMillis = 1_000,
                    completedAtMillis = null, notes = null, setCount = 0, totalVolume = 0.0, isPending = true,
                ),
            )
            database.workoutSessionDao().insertSetLog(
                WorkoutSetLogEntity(
                    sessionLocalId = localId, planItemId = 11, setNumber = 1,
                    reps = 8, weight = 60.0, durationSeconds = null,
                ),
            )

            assertEquals(1, database.workoutPlanDao().getItems(3).size)
            assertEquals(1, database.workoutSessionDao().getSetLogs(localId).size)
            // AUTOINCREMENT means the session got a generated id rather than the default 0.
            assertEquals(1L, localId)
            // Data cached before the upgrade is still there.
            assertEquals("Barbell Back Squat", database.exerciseDao().getById(5)?.name)
        } finally {
            database.close()
        }
    }

    /** Builds the schema as it stood at version 2, before any workout tables existed. */
    private fun seedVersion2Database() {
        val database = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DB_NAME), null)
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS exercises (id INTEGER NOT NULL, name TEXT NOT NULL, " +
                "description TEXT NOT NULL, categoryId INTEGER, categoryName TEXT, categorySlug TEXT, " +
                "tagsJson TEXT NOT NULL, videoUrl TEXT, PRIMARY KEY(id))",
        )
        database.execSQL("CREATE TABLE IF NOT EXISTS favorite_exercises (exerciseId INTEGER NOT NULL, PRIMARY KEY(exerciseId))")
        database.execSQL(
            "INSERT INTO exercises (id, name, description, tagsJson) VALUES (5, 'Barbell Back Squat', 'Squat.', '[]')",
        )
        database.version = 2
        database.close()
    }

    private companion object {
        const val DB_NAME = "workout-migration-test.db"
    }
}
