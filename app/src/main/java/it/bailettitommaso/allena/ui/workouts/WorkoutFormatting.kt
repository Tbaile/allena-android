package it.bailettitommaso.allena.ui.workouts

import it.bailettitommaso.allena.domain.model.WorkoutPlanItem
import java.time.Duration
import java.util.Locale
import kotlin.math.abs

/** "4 × 8" for a rep target, "3 × 45s" for a timed hold. */
fun WorkoutPlanItem.loadLabel(): String {
    val perSet = if (isTimed) "${durationSeconds}s" else reps?.toString() ?: "—"
    return "$sets × $perSet"
}

/** Rest between sets as "45s", "1:30" or "2:00". */
fun formatRest(seconds: Int): String =
    if (seconds < 60) "${seconds}s" else "%d:%02d".format(seconds / 60, seconds % 60)

fun formatWeight(kilograms: Double): String =
    if (kilograms % 1.0 == 0.0) "${kilograms.toInt()} kg" else "$kilograms kg"

/** Training volume, grouped for readability: "7,905 kg". */
fun formatVolume(kilograms: Double): String = "%,d kg".format(Locale.US, kilograms.toInt())

/** Time spent training as "52 min", or "5h 12m" once it passes the hour. */
fun formatDuration(duration: Duration): String {
    val minutes = duration.toMinutes()
    return if (minutes >= 60) "%dh %02dm".format(Locale.US, minutes / 60, minutes % 60) else "$minutes min"
}

/** Signed change against an earlier value: "+4.7%", "+46%", "−12%". */
fun formatPercentDelta(fraction: Double): String {
    val percent = fraction * 100
    val magnitude = if (abs(percent) >= 10) {
        "%.0f".format(Locale.US, abs(percent))
    } else {
        "%.1f".format(Locale.US, abs(percent))
    }

    return "${if (percent < 0) "−" else "+"}$magnitude%"
}
