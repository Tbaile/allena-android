package it.bailettitommaso.allena.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteExerciseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteExerciseEntity)

    @Delete
    suspend fun remove(favorite: FavoriteExerciseEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_exercises WHERE exerciseId = :exerciseId)")
    suspend fun isFavoriteNow(exerciseId: Long): Boolean

    @Query(
        "SELECT exercises.* FROM exercises " +
            "INNER JOIN favorite_exercises ON exercises.id = favorite_exercises.exerciseId " +
            "ORDER BY exercises.name",
    )
    fun favorites(): Flow<List<ExerciseEntity>>
}
