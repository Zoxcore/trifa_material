package org.briarproject.briar.desktop.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.jninotifications.NTFYActivity
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.getNativeLibGITHASH
import com.zoffcc.applications.trifa.MainActivity.Companion.getNativeLibTOXGITHASH
import com.zoffcc.applications.trifa.MainActivity.Companion.jnictoxcore_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libavutil_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libopus_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libvpx_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libsodium_version
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_version_major
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_version_minor
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_version_patch
import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import org.sqlite.SQLiteJDBCLoader

@Composable
fun AboutScreen(
    onBackButton: () -> Unit,
) = Box {
    AboutScreen()

    IconButton(
        icon = Icons.Filled.ArrowBack,
        contentDescription = i18n("access.return_to_previous_screen"),
        onClick = onBackButton,
        modifier = Modifier.align(TopStart)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutScreen(modifier: Modifier = Modifier.padding(16.dp)) {
    Column(modifier) {
        Row(
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TrifaLogo(modifier = Modifier.height(48.dp))
            Text(
                "TRIfA Material",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(start = 16.dp),
                maxLines = 1,
            )
        }
        var state by remember { mutableStateOf(0) }
        val titles = listOf(i18n("about.category.general"), i18n("about.category.dependencies"))
        TabRow(selectedTabIndex = state, backgroundColor = MaterialTheme.colors.background) {
            titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = state == index,
                    onClick = { state = index }
                )
            }
        }
        when (state) {
            0 -> GeneralInfo()
            1 -> Libraries()
        }
    }
}

@Composable
private fun GeneralInfo() {
    // format date
    // val commitTime = Instant.ofEpochMilli(BuildData.GIT_TIME).atZone(ZoneId.systemDefault()).toLocalDateTime()

    // rows displayed in table
    val lines = buildList {
        // add(Entry(i18n("about.copyright"), Strings.APP_AUTHORS))
        // add(Entry(i18n("about.license"), Strings.LICENSE))
        // add(Entry(i18n("about.version"), BuildData.VERSION))
        // add(Entry(i18n("about.version.core"), BuildData.CORE_VERSION))
        // if (BuildData.GIT_BRANCH != null) add(Entry("Git branch", BuildData.GIT_BRANCH)) // NON-NLS
        // if (BuildData.GIT_TAG != null) add(Entry("Git tag", BuildData.GIT_TAG)) // NON-NLS
        // if (BuildData.GIT_BRANCH == null && BuildData.GIT_TAG == null)
        //   add(Entry("Git branch/tag", "None detected")) // NON-NLS
        // add(Entry("Git hash", BuildData.GIT_HASH)) // NON-NLS
        // add(Entry("Commit time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(commitTime))) // NON-NLS
        add(Entry(i18n("about.trifa_material_version"), BuildConfig.APP_VERSION))
        add(Entry(i18n("about.git_commit_branch"), BuildConfig.GIT_BRANCH))
        add(Entry(i18n("about.git_commit_hash"), BuildConfig.GIT_COMMIT_HASH))
        add(Entry(i18n("about.git_commit_date"), BuildConfig.GIT_COMMIT_DATE))
        add(Entry(i18n("about.git_commit_msg"), BuildConfig.GIT_COMMIT_MSG))
        add(Entry(i18n("about.website"), "https://github.com/Zoxcore/trifa_material", true))

        add(Entry(i18n("about.toxcore_version"), "" + tox_version_major() +"."+ tox_version_minor()+"."+ tox_version_patch()))
        add(Entry(i18n("about.toxcore_commit_hash"), "" + getNativeLibTOXGITHASH()))

        add(Entry(i18n("about.jnictoxcore_version"), "" + jnictoxcore_version()))
        // add(Entry(i18n("about.jnictoxcore_commit_hash"), "" + getNativeLibGITHASH()))
        var libavutil_version = "???"
        var libopus_version = "???"
        var libvpx_version = "???"
        var libsodium_version = "???"
        try
        {
            libavutil_version = libavutil_version()!!
            libopus_version = libopus_version()!!
            libvpx_version = libvpx_version()!!
            libsodium_version = libsodium_version()!!
        }
        catch (_: Exception)
        {
        }
        add(Entry(i18n("about.libavutil_version"), libavutil_version))
        add(Entry(i18n("about.libopus_version"), libopus_version))
        add(Entry(i18n("about.libvpx_version"), libvpx_version))
        add(Entry(i18n("about.libsodium_version"), libsodium_version))
        var ffmpegav_libavutil_version = "???"
        var ffmpegav_version = "???"
        if (MainActivity.native_ffmpegav_lib_loaded_error == 0)
        {
            try
            {
                ffmpegav_libavutil_version = AVActivity.ffmpegav_libavutil_version()
                ffmpegav_version = AVActivity.ffmpegav_version()
            } catch (_: Exception)
            {
            }
        }
        else
        {
            ffmpegav_libavutil_version = "JNI lib not loaded"
            ffmpegav_version = "JNI lib not loaded"
        }
        add(Entry(i18n("about.ffmpegav_libavutil_version"), ffmpegav_libavutil_version))
        add(Entry(i18n("about.ffmpegav_version"), ffmpegav_version))
        var jninotifications_version = "???"

        if (MainActivity.native_notification_lib_loaded_error == 0)
        {
            try
            {
                jninotifications_version = NTFYActivity.jninotifications_version()
            } catch (_: Exception)
            {
            }
        }
        else
        {
            jninotifications_version = "JNI lib not loaded"
        }
        add(Entry(i18n("about.jninotifications_version"), jninotifications_version))
        add(Entry(i18n("about.sqlitejdbc"), SQLiteJDBCLoader.getVersion()))
        // add(Entry(i18n("about.contact"), Strings.EMAIL, true))
    }

    VerticallyScrollableArea { scrollState ->
        LazyColumn(
            modifier = Modifier.semantics {
                contentDescription = i18n("access.about.list.general")
            },
            state = scrollState
        ) {
            item {
                HorizontalDivider()
            }
            items(lines) {
                AboutEntry(it)
                HorizontalDivider()
            }
        }
    }
}

private data class Entry(
    val label: String,
    val value: String,
    val showCopy: Boolean = false,
)

// sizes of the two columns in the general tab
private val colSizesGeneral = listOf(0.3f, 0.7f)

@Composable
private fun AboutEntry(entry: Entry) =
    Row(
        Modifier
            .fillMaxWidth()
            // this is required for Divider between Boxes to have appropriate size
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {
                // manual text setting can be removed if Compose issue resolved
                // https://github.com/JetBrains/compose-jb/issues/2111
                text = buildAnnotatedString { append("${entry.label}: ${entry.value}") }
            }
    ) {
        Cell(colSizesGeneral[0], entry.label)
        VerticalDivider()
        Box(modifier = Modifier.weight(colSizesGeneral[1]).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = CenterVertically
            ) {
                SelectionContainer {
                    Text(
                        text = entry.value,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                if (entry.showCopy) {
                    val clipboardManager = LocalClipboardManager.current
                    IconButton(
                        icon = Icons.Filled.ContentCopy,
                        contentDescription = i18n("copy"),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(entry.value))
                        }
                    )
                }
            }
        }
    }

@Composable
private fun Libraries() {
    VerticallyScrollableArea { scrollState ->
        LazyColumn(
            modifier = Modifier.semantics {
                contentDescription = i18n("access.about.list.dependencies")
            },
            state = scrollState
        ) {
            item {
                HorizontalDivider()
            }
            /*
            items(BuildData.ARTIFACTS) { artifact ->
                LibraryEntry(artifact)
                HorizontalDivider()
            }
            */
        }
    }
}

// sizes of the four columns in the dependencies tab
val colSizesLibraries = listOf(0.3f, 0.3f, 0.15f, 0.25f)

/*
@Composable
private fun LibraryEntry(artifact: Artifact) =
    SelectionContainer {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Add tiny cells in between so that one can select a row, copy and paste
            // it somewhere and appear like "group:artifact:version license".
            Cell(colSizesLibraries[0], artifact.group)
            Cell(MIN_VALUE, ":")
            VerticalDivider()
            Cell(colSizesLibraries[1], artifact.artifact)
            Cell(MIN_VALUE, ":")
            VerticalDivider()
            Cell(colSizesLibraries[2], artifact.version)
            Cell(MIN_VALUE, "\t")
            VerticalDivider()
            Cell(colSizesLibraries[3], artifact.license)
        }
    }
*/

@Composable
private fun RowScope.Cell(size: Float, text: String) =
    Box(modifier = Modifier.weight(size).fillMaxHeight()) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(8.dp).align(CenterStart)
        )
    }
