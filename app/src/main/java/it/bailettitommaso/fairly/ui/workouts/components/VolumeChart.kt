package it.bailettitommaso.fairly.ui.workouts.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import it.bailettitommaso.fairly.ui.theme.FairlyTheme
import it.bailettitommaso.fairly.ui.workouts.WeeklyVolume
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ChartHeight = 160.dp
private val WeekLabelFormat = DateTimeFormatter.ofPattern("d MMM")

/**
 * Weekly training volume as a column per week. One series, so no legend: the caller's heading
 * names it.
 */
@Composable
fun VolumeChart(bars: List<WeeklyVolume>, modifier: Modifier = Modifier) {
    val peak = bars.maxOfOrNull { it.volume } ?: 0.0

    if (bars.isEmpty() || peak <= 0.0) {
        Text(
            text = "Log a workout to see your progress",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 24.dp),
        )
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(bars) {
        modelProducer.runTransaction {
            columnSeries { series(bars.map { it.volume }) }
        }
    }

    val weekLabels = CartesianValueFormatter { _, value, _ ->
        val bar = bars.getOrNull(value.toInt()) ?: bars.last()
        bar.weekStart.format(WeekLabelFormat)
    }

    // Eight dates across a phone would collide. Vico rejects blank labels, so the choice of which
    // weeks to label belongs to the item placer: spacing of lastIndex leaves the two ends.
    val endsOnly = remember(bars.size) {
        HorizontalAxis.ItemPlacer.aligned(spacing = { maxOf(bars.lastIndex, 1) })
    }

    // Without this the chart draws in Vico's own palette; the M3 theme maps it onto FairlyTheme.
    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = weekLabels, itemPlacer = endsOnly),
            ),
            modelProducer = modelProducer,
            modifier = modifier
                .fillMaxWidth()
                .height(ChartHeight)
                .semantics {
                    contentDescription = "Weekly training volume, peaking at ${peak.toInt()} kilograms"
                },
        )
    }
}

private val previewBars = listOf(
    WeeklyVolume(LocalDate.of(2026, 7, 6), 0.0),
    WeeklyVolume(LocalDate.of(2026, 7, 13), 3200.0),
    WeeklyVolume(LocalDate.of(2026, 7, 20), 4100.0),
    WeeklyVolume(LocalDate.of(2026, 7, 27), 3800.0),
    WeeklyVolume(LocalDate.of(2026, 8, 3), 0.0),
    WeeklyVolume(LocalDate.of(2026, 8, 10), 4600.0),
    WeeklyVolume(LocalDate.of(2026, 8, 17), 5200.0),
    WeeklyVolume(LocalDate.of(2026, 8, 24), 2400.0),
)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun VolumeChartPreview() {
    FairlyTheme {
        VolumeChart(bars = previewBars, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VolumeChartDarkPreview() {
    FairlyTheme {
        VolumeChart(bars = previewBars, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun VolumeChartEmptyPreview() {
    FairlyTheme {
        VolumeChart(bars = previewBars.map { it.copy(volume = 0.0) }, modifier = Modifier.padding(16.dp))
    }
}
