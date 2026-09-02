package it.bailettitommaso.allena.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface WorkoutPlanDao {
    @Upsert
    suspend fun upsertPlans(plans: List<WorkoutPlanEntity>)

    @Upsert
    suspend fun upsertItems(items: List<WorkoutPlanItemEntity>)

    @Query("SELECT * FROM workout_plans ORDER BY name")
    suspend fun getPlans(): List<WorkoutPlanEntity>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getPlan(id: Long): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plan_items WHERE planId = :planId ORDER BY position")
    suspend fun getItems(planId: Long): List<WorkoutPlanItemEntity>

    @Query("SELECT * FROM workout_plan_items WHERE id = :id")
    suspend fun getItem(id: Long): WorkoutPlanItemEntity?

    @Query("DELETE FROM workout_plan_items WHERE planId = :planId")
    suspend fun deleteItems(planId: Long)

    /** Replaces a plan's items wholesale so an exercise dropped upstream disappears locally too. */
    @Transaction
    suspend fun replacePlan(plan: WorkoutPlanEntity, items: List<WorkoutPlanItemEntity>) {
        upsertPlans(listOf(plan))
        deleteItems(plan.id)
        upsertItems(items)
    }
}
