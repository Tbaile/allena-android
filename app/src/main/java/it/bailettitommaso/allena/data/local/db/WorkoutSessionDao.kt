package it.bailettitommaso.allena.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert
    suspend fun insertSetLog(setLog: WorkoutSetLogEntity)

    @Insert
    suspend fun insertSessions(sessions: List<WorkoutSessionEntity>)

    @Query("SELECT * FROM workout_sessions ORDER BY startedAtMillis DESC")
    fun sessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE localId = :localId")
    suspend fun getSession(localId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE isPending = 1 ORDER BY startedAtMillis")
    suspend fun getPending(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_set_logs WHERE sessionLocalId = :sessionLocalId ORDER BY planItemId, setNumber")
    suspend fun getSetLogs(sessionLocalId: Long): List<WorkoutSetLogEntity>

    @Query(
        "UPDATE workout_sessions SET completedAtMillis = :completedAtMillis, notes = :notes, " +
            "setCount = :setCount, totalVolume = :totalVolume WHERE localId = :localId",
    )
    suspend fun markFinished(
        localId: Long,
        completedAtMillis: Long,
        notes: String?,
        setCount: Int,
        totalVolume: Double,
    )

    @Query("UPDATE workout_sessions SET remoteId = :remoteId, isPending = 0 WHERE localId = :localId")
    suspend fun markSynced(localId: Long, remoteId: Long)

    @Query("DELETE FROM workout_sessions WHERE localId = :localId")
    suspend fun deleteSession(localId: Long)

    @Query("DELETE FROM workout_sessions WHERE isPending = 0")
    suspend fun deleteSynced()

    /** Server history replaces the synced rows only; pending workouts are never dropped. */
    @Transaction
    suspend fun replaceSynced(sessions: List<WorkoutSessionEntity>) {
        deleteSynced()
        insertSessions(sessions)
    }
}
