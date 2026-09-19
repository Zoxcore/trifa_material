@file:Suppress("SpellCheckingInspection", "ConvertToStringTemplate", "RemoveSingleExpressionStringTemplate", "LocalVariableName")

package org.briarproject.briar.desktop.ui

import Theme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_id_bytes
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_id_count
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_total_bytes
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_netprof_get_packet_total_count
import com.zoffcc.applications.trifa.ToxVars
import globalstore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import kotlin.time.Duration.Companion.milliseconds

data class PacketStat(
    val id: ToxVars.TOX_NETPROF_PACKET_ID,
    val name: String,
    val sentCount: Long,
    val recvCount: Long,
    val sentBytes: Long,
    val recvBytes: Long
)

data class NetprofData(
    val totalSentCount: Long,
    val totalRecvCount: Long,
    val totalSentBytes: Long,
    val totalRecvBytes: Long,
    val packets: List<PacketStat>,
    val maxBytes: Long,
    val maxCount: Long
)

fun formatBytes(bytes: Long): String
{
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.2f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.2f GB", gb)
}

@Composable
fun RowScope.SummaryCard(title: String, bytes: String, packets: String, color: Color)
{
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.caption, color = color, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = bytes, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold, color = color)
        Text(text = "$packets packets", style = MaterialTheme.typography.body2, color = color.copy(alpha = 0.8f))
    }
}

@Composable
fun StatBar(sentValue: Long, recvValue: Long, maxValue: Long, sentColor: Color, recvColor: Color)
{
    val max = maxValue.coerceAtLeast(1L)
    val sentWeight = sentValue.toFloat().coerceAtLeast(0f)
    val recvWeight = recvValue.toFloat().coerceAtLeast(0f)
    val emptyWeight = (max - sentValue - recvValue).toFloat().coerceAtLeast(0f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Gray.copy(alpha = 0.1f))
    ) {
        if (sentWeight > 0f)
        {
            Box(modifier = Modifier.weight(sentWeight).fillMaxHeight().background(sentColor))
        }
        if (recvWeight > 0f)
        {
            Box(modifier = Modifier.weight(recvWeight).fillMaxHeight().background(recvColor))
        }
        if (emptyWeight > 0f)
        {
            Box(modifier = Modifier.weight(emptyWeight).fillMaxHeight())
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, DelicateCoroutinesApi::class)
@Composable
fun NetprofScreen(modifier: Modifier = Modifier.padding(16.dp))
{
    val global_store by globalstore.stateFlow.collectAsState()
    if (global_store.toxRunning)
    {
        var netprofData by remember { mutableStateOf<NetprofData?>(null) }

        LaunchedEffect(Unit) {
            while (isActive)
            {
                val data = withContext(Dispatchers.IO) {
                    // type 2 = TCP, type 3 = UDP
                    // direction 0 = SENT, direction 1 = RECV
                    val tcpSentCount = tox_netprof_get_packet_total_count(2, 0)
                    val tcpRecvCount = tox_netprof_get_packet_total_count(2, 1)
                    val udpSentCount = tox_netprof_get_packet_total_count(3, 0)
                    val udpRecvCount = tox_netprof_get_packet_total_count(3, 1)
                    val tcpSentBytes = tox_netprof_get_packet_total_bytes(2, 0)
                    val tcpRecvBytes = tox_netprof_get_packet_total_bytes(2, 1)
                    val udpSentBytes = tox_netprof_get_packet_total_bytes(3, 0)
                    val udpRecvBytes = tox_netprof_get_packet_total_bytes(3, 1)
                    val totalSentCount = tcpSentCount + udpSentCount
                    val totalRecvCount = tcpRecvCount + udpRecvCount
                    val totalSentBytes = tcpSentBytes + udpSentBytes
                    val totalRecvBytes = tcpRecvBytes + udpRecvBytes
                    val stats = ToxVars.TOX_NETPROF_PACKET_ID.values().map { id ->
                        val sC = tox_netprof_get_packet_id_count(2, id.value, 0) + tox_netprof_get_packet_id_count(3, id.value, 0)
                        val rC = tox_netprof_get_packet_id_count(2, id.value, 1) + tox_netprof_get_packet_id_count(3, id.value, 1)
                        val sB = tox_netprof_get_packet_id_bytes(2, id.value, 0) + tox_netprof_get_packet_id_bytes(3, id.value, 0)
                        val rB = tox_netprof_get_packet_id_bytes(2, id.value, 1) + tox_netprof_get_packet_id_bytes(3, id.value, 1)
                        PacketStat(id, id.name.removePrefix("TOX_NETPROF_PACKET_ID_"), sC, rC, sB, rB)
                    }.filter { it.sentCount > 0 || it.recvCount > 0 || it.sentBytes > 0 || it.recvBytes > 0 }
                        .sortedByDescending { it.sentBytes + it.recvBytes }
                    val maxBytes = stats.maxOfOrNull { it.sentBytes + it.recvBytes } ?: 1L
                    val maxCount = stats.maxOfOrNull { it.sentCount + it.recvCount } ?: 1L

                    NetprofData(totalSentCount, totalRecvCount, totalSentBytes, totalRecvBytes, stats, maxBytes, maxCount)
                }
                netprofData = data
                delay(1000.milliseconds)
            }
        }

        Column(modifier = modifier.fillMaxSize()) {
            Text(
                text = "Network Profiler",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            val data = netprofData
            if (data == null)
            {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Center) {
                    Text("Loading network statistics...", style = MaterialTheme.typography.h6)
                }
            } else
            {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard("Total Sent", formatBytes(data.totalSentBytes), data.totalSentCount.toString(), Color(0xFF2196F3))
                    SummaryCard("Total Received", formatBytes(data.totalRecvBytes), data.totalRecvCount.toString(), Color(0xFF4CAF50))
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Legend:", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.width(12.dp).height(12.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF2196F3)))
                    Text("Sent", fontSize = 12.sp)
                    Box(modifier = Modifier.width(12.dp).height(12.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF4CAF50)))
                    Text("Received", fontSize = 12.sp)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(data.packets) { stat ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colors.surface)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = stat.name,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            StatBar(stat.sentBytes, stat.recvBytes, data.maxBytes, Color(0xFF2196F3), Color(0xFF4CAF50))

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Sent: ${formatBytes(stat.sentBytes)} (${stat.sentCount} pkts)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2196F3)
                                )
                                Text(
                                    text = "Recv: ${formatBytes(stat.recvBytes)} (${stat.recvCount} pkts)",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    else
    {
        ExplainerToxNotRunning()
    }
}
