@file:Suppress("SpellCheckingInspection", "ConvertToStringTemplate", "RemoveSingleExpressionStringTemplate", "LocalVariableName")

package org.briarproject.briar.desktop.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_get_estimated_cpu_cycles
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_mid_get_network_stats
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_id_bytes
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_id_count
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_total_bytes
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_total_count
import com.zoffcc.applications.trifa.ToxVars
import globalstore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.log10
import kotlin.time.Duration.Companion.milliseconds

// --- Netprof Color Palette ---
// Note: Kotlin's `const val` only supports primitives and Strings.
// For Compose `Color`, we use top-level `val` to act as constants.
//
// HIGH-CONTRAST, THEME-ADAPTIVE ACCENTS for the summary cards:
//  - Light theme: deep, saturated tones (>= 7:1 contrast on white)
//  - Dark theme:  bright tones (>= 7:1 contrast on dark surfaces)
// The big numeric values additionally use MaterialTheme.colors.onSurface
// (pure theme foreground) so they are always maximally readable.
private val NetprofSentAccentLight = Color(0xFF0D47A1) // deep blue
private val NetprofSentAccentDark = Color(0xFF82B1FF)  // bright blue
private val NetprofRecvAccentLight = Color(0xFF1B5E20) // deep green
private val NetprofRecvAccentDark = Color(0xFF69F0AE)  // bright green
private val NetprofUptimeAccentLight = Color(0xFF00695C) // deep teal
private val NetprofUptimeAccentDark = Color(0xFF64FFDA)  // bright teal

private val NetprofColorMiddleware = Color(0xFF9C27B0)

private val NetprofColorHeatNone = Color(0xFF2C2C2E)
private val NetprofColorHeatLow1 = Color(0xFF1A237E)
private val NetprofColorHeatLow2 = Color(0xFF0288D1)
private val NetprofColorHeatMed = Color(0xFF4CAF50)
private val NetprofColorHeatHigh = Color(0xFFFFEB3B)
private val NetprofColorHeatMax = Color(0xFFF44336)
private val NetprofColorTextDark = Color.Black
private val NetprofColorTextLight = Color.White
// -----------------------------

@Composable
fun netprofAccent(lightVariant: Color, darkVariant: Color): Color =
    if (MaterialTheme.colors.isLight) lightVariant else darkVariant

data class PacketStat(
    val id: ToxVars.TOX_NETPROF_PACKET_ID,
    val name: String,
    val transport: String, // "TCP" or "UDP"
    val sentCount: Long,
    val recvCount: Long,
    val sentBytes: Long,
    val recvBytes: Long,
    val bytesPerSec: Long
)

data class NetprofData(
    val totalSentCount: Long,
    val totalRecvCount: Long,
    val totalSentBytes: Long,
    val totalRecvBytes: Long,
    val sentBytesPerSec: Long,
    val recvBytesPerSec: Long,
    val midSentBytes: Long,
    val midRecvBytes: Long,
    val midBytesPerSec: Long,
    val packets: List<PacketStat>,
    val uptimeMillis: Long,
    val cpuCycles: Long,
    val cpuCyclesPerSec: Long
)

// Holds the previous sample so we can compute per-second rates (deltas).
// packetBytes is keyed by "<PACKET_ID_NAME>@<TCP|UDP>" so TCP and UDP
// rates are tracked independently per packet type.
private class NetprofPrevStats {
    var initialized = false
    var lastTimestamp: Long = System.currentTimeMillis()
    var sentBytes: Long = 0L
    var recvBytes: Long = 0L
    var midBytes: Long = 0L
    var cpuCycles: Long = 0L
    val packetBytes = mutableMapOf<String, Long>()
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.2f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB", gb)
}

fun formatRate(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "0 B/s"
    if (bytesPerSec < 1024) return "$bytesPerSec B/s"
    val kb = bytesPerSec / 1024.0
    if (kb < 1024) return String.format("%.1f KB/s", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.2f MB/s", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB/s", gb)
}

fun formatCycles(cps: Long): String {
    if (cps <= 0) return "0 c/s"
    if (cps < 1000) return "$cps c/s"
    val k = cps / 1000.0
    if (k < 1000) return String.format("%.1f Kc/s", k)
    val m = k / 1000.0
    if (m < 1000) return String.format("%.1f Mc/s", m)
    val g = m / 1000.0
    return String.format("%.2f Gc/s", g)
}

fun formatUptime(millis: Long): String {
    if (millis < 0) return "00:00:00"
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Map a generic positive value to a 0..1 heat ratio using a logarithmic scale.
 */
fun valueToHeatRatio(value: Long, maxVal: Double): Float {
    if (value <= 0) return 0f
    val logMax = log10(maxVal)
    val logVal = log10(value.toDouble().coerceAtLeast(1.0))
    val ratio = (logVal / logMax).coerceIn(0.0, 1.0).toFloat()
    // any traffic/activity at all should at least show a tiny bit of heat
    return ratio.coerceAtLeast(0.05f)
}

/**
 * Map a bytes-per-second RATE to a 0..1 heat ratio using a logarithmic scale.
 * Cumulative byte totals must NOT be used for heat (they only grow, so everything
 * would permanently show "Max" heat).
 *
 * Scale (log10, 1 B/s .. 500 KiB/s):
 *   0 B/s        -> 0.00 (None)
 *   10 B/s       -> ~0.17 (Low)
 *   100 B/s      -> 0.35 (Low/Med boundary)
 *   1 KB/s       -> ~0.53 (Med)
 *   10 KB/s      -> 0.70 (Med/High boundary)
 *   100 KB/s     -> ~0.88 (High)
 *   >= 500 KiB/s -> 1.00 (Max / Red)
 */
fun rateToHeatRatio(bytesPerSec: Long): Float = valueToHeatRatio(bytesPerSec, 500.0 * 1024.0)

/**
 * Map CPU cycles per second to a 0..1 heat ratio.
 * Since baseline CPU usage for a desktop app doing crypto/networking is often
 * in the hundreds of millions of cycles per second (e.g. 600 Mc/s), we use a
 * shifted log scale from 10 Mc/s to 10 Gc/s.
 *
 * Scale:
 *   < 10 Mc/s    -> 0.00 (None/Idle)
 *   100 Mc/s     -> 0.33 (Green)
 *   600 Mc/s     -> 0.59 (Yellow/Orange - typical moderate load)
 *   1 Gc/s       -> 0.66 (Orange)
 *   10 Gc/s      -> 1.00 (Red / Max)
 */
fun cpuToHeatRatio(cps: Long): Float {
    if (cps <= 10_000_000L) return 0f // Below 10 Mc/s is essentially idle
    val logMax = 3.0 // 10 Mc/s to 10 Gc/s is 3 decades (log10(1000) = 3)
    val logVal = log10(cps.toDouble() / 10_000_000.0)
    val ratio = (logVal / logMax).coerceIn(0.0, 1.0).toFloat()
    return ratio.coerceAtLeast(0.05f)
}

fun lerpColor(c1: Color, c2: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = c1.red + (c2.red - c1.red) * f,
        green = c1.green + (c2.green - c1.green) * f,
        blue = c1.blue + (c2.blue - c1.blue) * f,
        alpha = c1.alpha + (c2.alpha - c1.alpha) * f
    )
}

fun getHeatColor(ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return when {
        r == 0f -> NetprofColorHeatNone
        r < 0.25f -> lerpColor(NetprofColorHeatLow1, NetprofColorHeatLow2, r / 0.25f)
        r < 0.5f -> lerpColor(NetprofColorHeatLow2, NetprofColorHeatMed, (r - 0.25f) / 0.25f)
        r < 0.75f -> lerpColor(NetprofColorHeatMed, NetprofColorHeatHigh, (r - 0.5f) / 0.25f)
        else -> lerpColor(NetprofColorHeatHigh, NetprofColorHeatMax, (r - 0.75f) / 0.25f)
    }
}

@Composable
fun RowScope.SummaryCard(title: String, bytes: String, packets: String, rate: String, accent: Color) {
    val isLight = MaterialTheme.colors.isLight
    // Maximum contrast for the important numbers: pure theme foreground color
    val mainText = MaterialTheme.colors.onSurface
    val secondaryText = mainText.copy(alpha = 0.85f)
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = if (isLight) 0.10f else 0.20f))
            .padding(12.dp)
    ) {
        Text(text = title, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(text = bytes, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = mainText)
        Text(text = rate, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = "$packets pkts", color = secondaryText, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}

@Composable
fun UptimeCard(title: String, uptime: String, accent: Color) {
    val isLight = MaterialTheme.colors.isLight
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = if (isLight) 0.10f else 0.20f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = accent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Text(text = uptime, color = MaterialTheme.colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NetprofScreen(modifier: Modifier = Modifier.padding(16.dp)) {
    val global_store by globalstore.stateFlow.collectAsState()
    if (global_store.toxRunning) {
        var netprofData by remember { mutableStateOf<NetprofData?>(null) }
        val prevStats = remember { NetprofPrevStats() }
        val startTs = global_store.toxStartedTimestamp

        LaunchedEffect(Unit) {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val deltaTimeSec = (currentTime - prevStats.lastTimestamp) / 1000.0

                val data = withContext(Dispatchers.IO) {
                    val typeTcp = ToxVars.TOX_NETPROF_PACKET_TYPE.TOX_NETPROF_PACKET_TYPE_TCP.value
                    val typeUdp = ToxVars.TOX_NETPROF_PACKET_TYPE.TOX_NETPROF_PACKET_TYPE_UDP.value
                    val dirSent = ToxVars.TOX_NETPROF_DIRECTION.TOX_NETPROF_DIRECTION_SENT.value
                    val dirRecv = ToxVars.TOX_NETPROF_DIRECTION.TOX_NETPROF_DIRECTION_RECV.value

                    val tcpSentCount = tox_netprof_get_packet_total_count(typeTcp, dirSent)
                    val tcpRecvCount = tox_netprof_get_packet_total_count(typeTcp, dirRecv)
                    val udpSentCount = tox_netprof_get_packet_total_count(typeUdp, dirSent)
                    val udpRecvCount = tox_netprof_get_packet_total_count(typeUdp, dirRecv)

                    val tcpSentBytes = tox_netprof_get_packet_total_bytes(typeTcp, dirSent)
                    val tcpRecvBytes = tox_netprof_get_packet_total_bytes(typeTcp, dirRecv)
                    val udpSentBytes = tox_netprof_get_packet_total_bytes(typeUdp, dirSent)
                    val udpRecvBytes = tox_netprof_get_packet_total_bytes(typeUdp, dirRecv)

                    val totalSentCount = tcpSentCount + udpSentCount
                    val totalRecvCount = tcpRecvCount + udpRecvCount
                    val totalSentBytes = tcpSentBytes + udpSentBytes
                    val totalRecvBytes = tcpRecvBytes + udpRecvBytes

                    val midStats = tox_group_mid_get_network_stats()
                    val midSentBytes = midStats?.getOrNull(0) ?: 0L
                    val midRecvBytes = midStats?.getOrNull(1) ?: 0L
                    val midTotalBytes = midSentBytes + midRecvBytes

                    val cpuCyclesRaw = tox_get_estimated_cpu_cycles()
                    val cpuCycles = if (cpuCyclesRaw < 0) 0L else cpuCyclesRaw

                    // On the very first sample we have no baseline yet, so rates are 0
                    // (otherwise the first tick would report the whole cumulative total as "rate").
                    val first = !prevStats.initialized

                    fun rateOf(deltaBytes: Long): Long {
                        if (first || deltaTimeSec <= 0.0) return 0L
                        return (deltaBytes.coerceAtLeast(0L) / deltaTimeSec).toLong()
                    }

                    val sentBps = rateOf(totalSentBytes - prevStats.sentBytes)
                    val recvBps = rateOf(totalRecvBytes - prevStats.recvBytes)
                    val midBps = rateOf(midTotalBytes - prevStats.midBytes)
                    val cpuCyclesPerSec = rateOf(cpuCycles - prevStats.cpuCycles)

                    // One box per (packet ID, transport): TCP and UDP separately
                    val stats = ToxVars.TOX_NETPROF_PACKET_ID.entries.flatMap { id ->
                        listOf(typeTcp to "TCP", typeUdp to "UDP").map { (typeVal, transportLabel) ->
                            val sC = tox_netprof_get_packet_id_count(typeVal, id.value, dirSent)
                            val rC = tox_netprof_get_packet_id_count(typeVal, id.value, dirRecv)
                            val sB = tox_netprof_get_packet_id_bytes(typeVal, id.value, dirSent)
                            val rB = tox_netprof_get_packet_id_bytes(typeVal, id.value, dirRecv)
                            val totalBytesNow = sB + rB
                            val mapKey = "${id.name}@$transportLabel"
                            val prevB = prevStats.packetBytes[mapKey] ?: 0L
                            val bps = rateOf(totalBytesNow - prevB)
                            prevStats.packetBytes[mapKey] = totalBytesNow
                            PacketStat(id, id.name.removePrefix("TOX_NETPROF_PACKET_ID_"), transportLabel, sC, rC, sB, rB, bps)
                        }
                    }

                    prevStats.initialized = true
                    prevStats.sentBytes = totalSentBytes
                    prevStats.recvBytes = totalRecvBytes
                    prevStats.midBytes = midTotalBytes
                    prevStats.cpuCycles = cpuCycles
                    prevStats.lastTimestamp = currentTime

                    val uptimeMillis = currentTime - startTs

                    NetprofData(
                        totalSentCount, totalRecvCount, totalSentBytes, totalRecvBytes,
                        sentBps, recvBps, midSentBytes, midRecvBytes, midBps, stats, uptimeMillis, cpuCycles, cpuCyclesPerSec
                    )
                }
                netprofData = data
                delay(1000.milliseconds)
            }
        }

        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Tox Profiler",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            val data = netprofData
            if (data == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
                    Text("Loading network statistics...", style = MaterialTheme.typography.h6)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryCard(
                        "Total Sent",
                        formatBytes(data.totalSentBytes),
                        data.totalSentCount.toString(),
                        formatRate(data.sentBytesPerSec),
                        netprofAccent(NetprofSentAccentLight, NetprofSentAccentDark)
                    )
                    SummaryCard(
                        "Total Received",
                        formatBytes(data.totalRecvBytes),
                        data.totalRecvCount.toString(),
                        formatRate(data.recvBytesPerSec),
                        netprofAccent(NetprofRecvAccentLight, NetprofRecvAccentDark)
                    )
                    UptimeCard(
                        "Tox Uptime",
                        formatUptime(data.uptimeMillis),
                        netprofAccent(NetprofUptimeAccentLight, NetprofUptimeAccentDark)
                    )
                }

                // Overall Network & CPU Heat Bars:
                // COLOR is logarithmic (turns red quickly at low speeds)
                // WIDTH is linear (only fills the whole bar when hitting the max threshold)
                val sentHeatRatio = rateToHeatRatio(data.sentBytesPerSec)
                val recvHeatRatio = rateToHeatRatio(data.recvBytesPerSec)

                // CPU cycles: 600 Mc/s is a normal moderate load for a desktop app.
                // We use a shifted log scale (10 Mc/s to 10 Gc/s) for color,
                // and a linear scale up to 3 Gc/s for the bar width so it doesn't look half-full.
                val maxCpsLinear = 3_000_000_000.0
                val cpuHeatRatio = cpuToHeatRatio(data.cpuCyclesPerSec)

                val sentHeatColor = getHeatColor(sentHeatRatio)
                val recvHeatColor = getHeatColor(recvHeatRatio)
                val cpuHeatColor = getHeatColor(cpuHeatRatio)

                val maxBpsLinear = 500.0 * 1024.0 // 500 KiB/s
                val sentWidthRatio = if (data.sentBytesPerSec > 0) (data.sentBytesPerSec / maxBpsLinear).coerceIn(0.02, 1.0).toFloat() else 0f
                val recvWidthRatio = if (data.recvBytesPerSec > 0) (data.recvBytesPerSec / maxBpsLinear).coerceIn(0.02, 1.0).toFloat() else 0f
                val cpuWidthRatio = if (data.cpuCyclesPerSec > 0) (data.cpuCyclesPerSec / maxCpsLinear).coerceIn(0.02, 1.0).toFloat() else 0f

                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Sent Heat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(70.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NetprofColorHeatNone)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(sentWidthRatio)
                                    .background(sentHeatColor)
                            )
                        }
                        Text(formatRate(data.sentBytesPerSec), fontSize = 11.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Recv Heat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(70.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NetprofColorHeatNone)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(recvWidthRatio)
                                    .background(recvHeatColor)
                            )
                        }
                        Text(formatRate(data.recvBytesPerSec), fontSize = 11.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("CPU Heat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(70.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NetprofColorHeatNone)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(cpuWidthRatio)
                                    .background(cpuHeatColor)
                            )
                        }
                        Text(formatCycles(data.cpuCyclesPerSec), fontSize = 11.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Packet Heat (log scale):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Box(modifier = Modifier.width(16.dp).height(10.dp).background(NetprofColorHeatNone, RoundedCornerShape(2.dp)))
                    Text("0 B/s", fontSize = 9.sp)
                    Box(modifier = Modifier.width(16.dp).height(10.dp).background(NetprofColorHeatLow1, RoundedCornerShape(2.dp)))
                    Text("~100 B/s", fontSize = 9.sp)
                    Box(modifier = Modifier.width(16.dp).height(10.dp).background(NetprofColorHeatMed, RoundedCornerShape(2.dp)))
                    Text("~1 KB/s", fontSize = 9.sp)
                    Box(modifier = Modifier.width(16.dp).height(10.dp).background(NetprofColorHeatHigh, RoundedCornerShape(2.dp)))
                    Text("~50 KB/s", fontSize = 9.sp)
                    Box(modifier = Modifier.width(16.dp).height(10.dp).background(NetprofColorHeatMax, RoundedCornerShape(2.dp)))
                    Text("≥500 KB/s", fontSize = 9.sp)
                }

                val scrollState = rememberScrollState()
                Box(modifier = Modifier.fillMaxSize()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // smaller boxes: we now have 2x as many (TCP + UDP per packet ID)
                        val itemModifier = Modifier.width(118.dp).height(64.dp)

                        val totalMidBytes = data.midSentBytes + data.midRecvBytes
                        val tooltipTextMid = buildString {
                            appendLine("Middleware Custom Packets (NGC)")
                            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
                            appendLine("Sent: ${formatBytes(data.midSentBytes)}")
                            appendLine("Recv: ${formatBytes(data.midRecvBytes)}")
                            appendLine("Rate: ${formatRate(data.midBytesPerSec)}")
                            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
                            appendLine("Total: ${formatBytes(totalMidBytes)}")
                        }

                        Tooltip(text = tooltipTextMid) {
                            Column(
                                modifier = itemModifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NetprofColorMiddleware)
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "MIDDLEWARE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NetprofColorTextLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Text(
                                        text = "NGC",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NetprofColorTextLight.copy(alpha = 0.7f)
                                    )
                                }
                                Column {
                                    Text(
                                        text = formatBytes(totalMidBytes),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NetprofColorTextLight
                                    )
                                    Text(
                                        text = "S: ${formatBytes(data.midSentBytes)} | R: ${formatBytes(data.midRecvBytes)}",
                                        fontSize = 8.sp,
                                        color = NetprofColorTextLight.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        data.packets.forEach { stat ->
                            val totalBytes = stat.sentBytes + stat.recvBytes
                            val totalPkts = stat.sentCount + stat.recvCount

                            // Heat is based on the CURRENT RATE (bytes/sec) of THIS transport, log-scaled
                            val heatRatio = rateToHeatRatio(stat.bytesPerSec)
                            val bgColor = getHeatColor(heatRatio)

                            val textColor = if (heatRatio > 0.6f) NetprofColorTextDark else NetprofColorTextLight

                            val tooltipText = buildString {
                                appendLine("Packet: ${stat.name}")
                                appendLine("Transport: ${stat.transport}")
                                appendLine("ID: 0x${stat.id.value.toString(16).uppercase().padStart(2, '0')}")
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("Sent: ${stat.sentCount} pkts (${formatBytes(stat.sentBytes)})")
                                appendLine("Recv: ${stat.recvCount} pkts (${formatBytes(stat.recvBytes)})")
                                appendLine("Rate: ${formatRate(stat.bytesPerSec)}")
                                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━")
                                appendLine("Total: $totalPkts pkts (${formatBytes(totalBytes)})")
                            }

                            Tooltip(text = tooltipText) {
                                Column(
                                    modifier = itemModifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bgColor)
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = stat.name,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Text(
                                            text = stat.transport,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = formatBytes(totalBytes),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor
                                        )
                                        Text(
                                            text = "${formatRate(stat.bytesPerSec)} | $totalPkts pkts",
                                            fontSize = 8.sp,
                                            color = textColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    CustomVerticalScrollbar2(
                        scrollState = scrollState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    } else {
        ExplainerToxNotRunning()
    }
}

@Composable
fun CustomVerticalScrollbar2(
    scrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier
) {
    if (scrollState.maxValue > 0) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = modifier
                .fillMaxHeight()
                .width(6.dp)
                .padding(vertical = 4.dp)
        ) {
            val totalContent = scrollState.maxValue + scrollState.viewportSize
            if (totalContent > 0 && scrollState.viewportSize > 0) {
                val density = androidx.compose.ui.platform.LocalDensity.current
                val trackHeightPx = with(density) { maxHeight.toPx() }
                val thumbHeightPx = (scrollState.viewportSize.toFloat() / totalContent) * trackHeightPx
                val thumbOffsetPx = (scrollState.value.toFloat() / totalContent) * trackHeightPx

                val thumbHeight = thumbHeightPx / density.density
                val thumbOffset = thumbOffsetPx / density.density

                val draggableDistance = trackHeightPx - thumbHeightPx
                val scrollRatio = if (draggableDistance > 0) scrollState.maxValue.toFloat() / draggableDistance else 0f

                Box(
                    modifier = Modifier
                        .offset(y = thumbOffset.dp)
                        .height(thumbHeight.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        .pointerInput(scrollState.maxValue, trackHeightPx, scrollRatio) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                if (scrollRatio > 0f) {
                                    val scrollDelta = dragAmount.y * scrollRatio
                                    scrollState.dispatchRawDelta(scrollDelta)
                                }
                            }
                        }
                )
            }
        }
    }
}
