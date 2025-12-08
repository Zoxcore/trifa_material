@file:Suppress("SpellCheckingInspection", "ConvertToStringTemplate", "RemoveSingleExpressionStringTemplate", "LocalVariableName")

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.sun.jndi.toolkit.url.Uri
import com.zoffcc.applications.ffmpegav.AVActivity
import com.zoffcc.applications.jninotifications.NTFYActivity
import com.zoffcc.applications.sorm.OrmaDatabase
import com.zoffcc.applications.sorm.OrmaDatabase.get_current_sqlite_version
import com.zoffcc.applications.trifa.HelperGeneric.get_java_os_name
import com.zoffcc.applications.trifa.HelperGeneric.get_java_os_version
import com.zoffcc.applications.trifa.HelperGeneric.get_trifa_build_str
import com.zoffcc.applications.trifa.HelperOSFile
import com.zoffcc.applications.trifa.Log
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.getNativeLibGITHASH
import com.zoffcc.applications.trifa.MainActivity.Companion.getNativeLibTOXGITHASH
import com.zoffcc.applications.trifa.MainActivity.Companion.jnictoxcore_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libavutil_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libopus_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libsodium_version
import com.zoffcc.applications.trifa.MainActivity.Companion.libvpx_version
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_number_groups
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_version_major
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_version_minor
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_version_patch
import com.zoffcc.applications.trifa.MainActivity.Companion.x264_version
import com.zoffcc.applications.trifa.TAG
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_GITHUB_NEW_ISSUE_URL
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import com.zoffcc.applications.trifa_material.trifa_material.BuildConfig
import globalstore
import kotlinx.coroutines.DelicateCoroutinesApi
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import org.sqlite.SQLiteJDBCLoader
import java.net.URLEncoder

@Composable
fun AboutScreen(
    onBackButton: () -> Unit,
) = Box {
    AboutScreen()

    IconButton(
        icon = Icons.Filled.ArrowBack,
        contentDescription = i18n("ui.return_to_previous_screen"),
        onClick = onBackButton,
        modifier = Modifier.align(TopStart)
    )
}

@OptIn(ExperimentalFoundationApi::class, DelicateCoroutinesApi::class)
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

        var show_link_click by remember { mutableStateOf(false) }
        var link_str by remember { mutableStateOf("") }
        show_report_bug_dialog(show_link_click, link_str) { show_link_click_, link_str_ ->
            show_link_click = show_link_click_
            link_str = link_str_
        }


        Row(Modifier.wrapContentHeight().padding(start = 15.dp)) {
            Button(modifier = Modifier.width(200.dp),
                enabled = true,
                onClick = {
                    try
                    {
                        var url: String = TRIFA_GITHUB_NEW_ISSUE_URL
                        url = "$url?labels=bug"
                        url = "$url&title=Bug:%20"
                        url = "$url&template=bug.yaml"
                        url = try
                        {
                            url + "&trifa_material_version=" + URLEncoder.encode(BuildConfig.APP_VERSION, "UTF-8")
                        } catch (e: java.lang.Exception)
                        {
                            "$url" + "&trifa_material_version=unknown"
                        }

                        url = try
                        {
                            url + "&os_detail=" + URLEncoder.encode(get_java_os_name() + " " + get_java_os_version(), "UTF-8")
                        } catch (e: java.lang.Exception)
                        {
                            "$url" + "&os_detail=unknown"
                        }

                        url = try
                        {
                            url + "&build=" + URLEncoder.encode(get_trifa_build_str(), "UTF-8")
                        } catch (e: java.lang.Exception)
                        {
                            "$url" + "&build=unknown"
                        }
                        // Log.i(TAG, "GITHUB_NEW_ISSUE_URL:" + url)
                        link_str = url
                        show_link_click = true
                    }
                    catch(e: Exception)
                    {
                    }
                })
            {
                Text(i18n("ui.about.report_bug"))
            }
        }

        var state by remember { mutableStateOf(0) }
        val titles = listOf(i18n("ui.about.category_general"), i18n("ui.about.category_dependencies"))
        /*
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
        */
        GeneralInfo()
    }
}

@Composable
private fun GeneralInfo() {
    // format date
    // val commitTime = Instant.ofEpochMilli(BuildData.GIT_TIME).atZone(ZoneId.systemDefault()).toLocalDateTime()

    val global_store by globalstore.stateFlow.collectAsState()
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
        if (global_store.ormaRunning)
        {
            try
            {
                add(Entry(i18n("about.number_of_friend_messages"), orma!!.selectFromMessage().count().toString()))
            } catch (_: Exception)
            {
            }
            try
            {
                add(Entry(i18n("about.number_of_group_messages"), orma!!.selectFromGroupMessage().count().toString()))
            } catch (_: Exception)
            {
            }
            try
            {
                add(Entry(i18n("about.number_of_friends"), orma!!.selectFromFriendList().count().toString()))
            } catch (_: Exception)
            {
            }
        }

        try
        {
            if (global_store.toxRunning)
            {
                // add(Entry(i18n("about.number_of_groups"), orma!!.selectFromGroupDB().count().toString()))
                add(Entry(i18n("about.number_of_groups"), tox_group_get_number_groups().toString()))
            }
        } catch (_: Exception)
        {
        }


        try
        {
            add(Entry(i18n("about.compose_version"), BuildConfig.COMPOSE_VERSION))
        } catch(_: Exception) {}
        try
        {
            add(Entry(i18n("about.kotlin_compiler_used_version"), BuildConfig.KOTLIN_VERSION))
        } catch(_: Exception) {}
        try
        {
            add(Entry(i18n("about.kotlin_runtime_version"), kotlin.KotlinVersion.CURRENT.toString()))
        } catch(_: Exception) {}
        try
        {
            add(Entry(i18n("about.java_runtime_version"), Runtime.version().toString()))
        } catch(_: Exception) {}

        add(Entry(i18n("about.toxcore_version"), "" + tox_version_major() +"."+ tox_version_minor()+"."+ tox_version_patch()))
        add(Entry(i18n("about.toxcore_commit_hash"), "" + getNativeLibTOXGITHASH()))

        add(Entry(i18n("about.jnictoxcore_version"), "" + jnictoxcore_version()))
        add(Entry(i18n("about.jnictoxcore_commit_hash"), "" + getNativeLibGITHASH()))
        var libavutil_version = "???"
        var libopus_version = "???"
        var libvpx_version = "???"
        var x264_version = "???"
        var libsodium_version = "???"
        try
        {
            libavutil_version = libavutil_version()!!
            libopus_version = libopus_version()!!
            libvpx_version = libvpx_version()!!
            x264_version = x264_version()!!
            libsodium_version = libsodium_version()!!
        }
        catch (_: Exception)
        {
        }
        add(Entry(i18n("about.libavutil_version"), libavutil_version))
        add(Entry(i18n("about.libopus_version"), libopus_version))
        add(Entry(i18n("about.libvpx_version"), libvpx_version))
        add(Entry(i18n("about.x264_version"), x264_version))
        add(Entry(i18n("about.libsodium_version"), libsodium_version))
        var ffmpegav_libavutil_version = "???"
        var ffmpegav_version = "???"
        var ffmpegav_git_hash = "???"
        if (MainActivity.native_ffmpegav_lib_loaded_error == 0)
        {
            try
            {
                ffmpegav_libavutil_version = AVActivity.ffmpegav_libavutil_version()
                ffmpegav_version = AVActivity.ffmpegav_version()
                ffmpegav_git_hash = AVActivity.ffmpegav_GITHASH()
            } catch (_: Exception)
            {
            }
        }
        else
        {
            ffmpegav_libavutil_version = "JNI lib not loaded"
            ffmpegav_version = "JNI lib not loaded"
            ffmpegav_git_hash = "JNI lib not loaded"
        }
        add(Entry(i18n("about.ffmpegav_libavutil_version"), ffmpegav_libavutil_version))
        add(Entry(i18n("about.ffmpegav_version"), ffmpegav_version))
        add(Entry(i18n("about.ffmpegav_commit_hash"), ffmpegav_git_hash))
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
        add(Entry(i18n("about.sqlitejdbc"), get_current_sqlite_version())) //SQLiteJDBCLoader.getVersion()))

        var debug__cipher_version: String? = "unknown"
        try
        {
            debug__cipher_version = OrmaDatabase.run_query_for_single_result("PRAGMA cipher_version")
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        if (!debug__cipher_version.isNullOrEmpty())
        {
            add(Entry(i18n("about.sqlcipher"), debug__cipher_version))
        }

        // add(Entry(i18n("about.contact"), Strings.EMAIL, true))
    }

    VerticallyScrollableArea { scrollState ->
        LazyColumn(
            modifier = Modifier.semantics {
                contentDescription = i18n("ui.about.list_general")
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
                        contentDescription = i18n("ui.copy"),
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
                contentDescription = i18n("ui.about.list_dependencies")
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

@Composable
fun show_report_bug_dialog(show_link_click: Boolean, link_str: String, setLinkVars: (Boolean, String) -> Unit)
{
    var show_link_click1 = show_link_click
    var link_str1 = link_str
    if (show_link_click1)
    {
        AlertDialog(onDismissRequest = { link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) },
            title = { Text("Open this URL ?") },
            confirmButton = {
                Button(onClick = { HelperOSFile.open_webpage(link_str1); link_str1 = ""; show_link_click1 = false; setLinkVars(show_link_click1, link_str1) }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { link_str1 = ""; show_link_click1 = false;setLinkVars(show_link_click1, link_str1) }) {
                    Text("No")
                }
            },
            text = { Text("This could be potentially dangerous!" + "\n\n" + link_str1) })
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
