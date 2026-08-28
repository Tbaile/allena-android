package it.bailettitommaso.fairly.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Upsert
    suspend fun upsertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises ORDER BY name")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<ExerciseEntity>

    @Query(
        "SELECT * FROM exercises " +
            "WHERE (:search IS NULL OR name LIKE '%' || :search || '%') " +
            "AND (:categorySlug IS NULL OR categorySlug = :categorySlug) " +
            "ORDER BY name",
    )
    suspend fun search(search: String?, categorySlug: String?): List<ExerciseEntity>
}
