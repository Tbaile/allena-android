package it.bailettitommaso.fairly.domain.model

data class WorkoutPlan(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val items: List<WorkoutPlanItem> = emptyList(),
)
