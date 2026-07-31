@file:Suppress("LocalVariableName")

package com.zoffcc.applications.trifa

import SETTINGS_HEADER_SIZE
import SnackBarToast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperGeneric.get_self_group_role
import com.zoffcc.applications.trifa.HelperGeneric.is_self_group_role_founder
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.dump_saved_offline_peers_to_log
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_founder_set_voice_state
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_voice_state
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_peer_id
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_set_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_name
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import org.briarproject.briar.desktop.DetailItem
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GroupSettingDetails(selectedGroupId: String?)
{
    SettingDetail(i18n("ui.group_settings_headline")) {

        // ---- change own name for group chats ----
        var self_name_in_group = ""
        try
        {
            val group_num = HelperGroup.tox_group_by_groupid__wrapper(selectedGroupId)
            val self_peernum = tox_group_self_get_peer_id(group_num)
            self_name_in_group = MainActivity.tox_group_peer_get_name(group_num, self_peernum)!!
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
        var tox_own_name_in_group by remember { mutableStateOf(self_name_in_group) }

        if (!selectedGroupId.isNullOrEmpty())
        {
            GroupDetailItem(selectable_text = true,
                label = "Group ID: " + selectedGroupId.lowercase(), description = "ID of this group")
            Spacer(modifier = Modifier.height(5.dp))
        }

        // ---- notifications of this group ----
        var group_notification_silent by remember { mutableStateOf(false) }
        try
        {
            if (orma!!.selectFromGroupDB().group_identifierEq(selectedGroupId).get(0).notification_silent)
            {
                group_notification_silent = true
            }
        } catch (_: Exception)
        {
        }
        DetailItem(label = i18n("ui.group_settings.notification_silent"),
            description = (if (group_notification_silent) i18n("enabled") else i18n("disabled"))) {
            Switch(
                checked = group_notification_silent,
                onCheckedChange = {
                    group_notification_silent = it
                    try
                    {
                        orma!!.updateGroupDB().group_identifierEq(selectedGroupId).notification_silent(group_notification_silent).execute()
                    }
                    catch(_: Exception)
                    {
                    }
                },
            )
        }
        // ---- notifications of this group ----

        var group_voice_state by remember { mutableStateOf(ToxVars.Tox_Group_Voice_State.TOX_GROUP_VOICE_STATE_ALL.value) }
        try
        {
            val group_num = HelperGroup.tox_group_by_groupid__wrapper(selectedGroupId!!.lowercase())
            val group_voice_state_tmp = tox_group_get_voice_state(group_num)
            if ((group_voice_state_tmp >= ToxVars.Tox_Group_Voice_State.TOX_GROUP_VOICE_STATE_ALL.value)
                && (group_voice_state_tmp <= ToxVars.Tox_Group_Voice_State.TOX_GROUP_VOICE_STATE_FOUNDER.value))
            {
                group_voice_state = group_voice_state_tmp
            }
        } catch (_: Exception)
        {
        }

        var expanded by remember { mutableStateOf(false) }
        val options = listOf("All", "Moderator", "Founder")
        var selectedVoicestate by remember { mutableStateOf(options[group_voice_state]) }

        DetailItem(
            label = i18n("ui.group_settings.voice_state"),
            description = selectedVoicestate
        ) {
            var is_admin = false
            try
            {
                is_admin = is_self_group_role_founder(get_self_group_role(selectedGroupId!!.lowercase()))
            }
            catch (_: Exception)
            {
            }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (is_admin) expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedVoicestate,
                    onValueChange = {},
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                try
                                {
                                    val is_admin = is_self_group_role_founder(get_self_group_role(selectedGroupId!!.lowercase()))
                                    if (is_admin)
                                    {
                                        selectedVoicestate = selectionOption
                                        expanded = false
                                        var new_value = ToxVars.Tox_Group_Voice_State.TOX_GROUP_VOICE_STATE_ALL.value
                                        val group_num = HelperGroup.tox_group_by_groupid__wrapper(selectedGroupId!!.lowercase())
                                        if (selectedVoicestate == "Founder")
                                        {
                                            new_value = ToxVars.Tox_Group_Voice_State.TOX_GROUP_VOICE_STATE_FOUNDER.value
                                        } else if (selectedVoicestate == "Moderator")
                                        {
                                            new_value = ToxVars.Tox_Group_Voice_State.TOX_GROUP_VOICE_STATE_MODERATOR.value
                                        }
                                        tox_group_founder_set_voice_state(group_num, new_value)
                                        update_savedata_file_wrapper()
                                    }
                                } catch (_: Exception)
                                {
                                }
                            }
                        )
                    }
                }
            }
        }









        var num_messages = "?"
        try
        {
            num_messages = "" + TrifaToxService.orma!!.selectFromGroupMessage().group_identifierEq(selectedGroupId!!.lowercase()).count()
        }
        catch(_: Exception)
        {
        }
        GroupDetailItem(label = "Number of Messages: " + num_messages, description = "Number of Messages in this group")
        Spacer(modifier = Modifier.height(5.dp))
        Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
            TextField(enabled = true, singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(0.dp).weight(1.0f),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                ), value = tox_own_name_in_group,
                onValueChange = {
                    tox_own_name_in_group = it
                })

            Button(modifier = Modifier.width(300.dp).padding(start = 20.dp, end = 20.dp),
                enabled = true,
                onClick = {
                    val group_num = HelperGroup.tox_group_by_groupid__wrapper(selectedGroupId)
                    if (group_num != -1L)
                    {
                        if (tox_group_self_set_name(group_num, tox_own_name_in_group) == 1)
                        {
                            HelperGeneric.update_savedata_file_wrapper()
                            SnackBarToast("You have changed your own peer name")
                        } else
                        {
                            SnackBarToast("Error while trying to set your own peer name")
                        }
                    }
                    else
                    {
                        SnackBarToast("Error while trying to set your own peer name")
                    }
                })
            {
                Text("Update your Peer Name")
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
            Button(modifier = Modifier.width(400.dp),
                enabled = true,
                onClick = {
                    dump_saved_offline_peers_to_log(selectedGroupId)
                })
            {
                Text("DEBUG: Dump saved offline peers to logfile")
            }
        }
    }
}

@Composable
fun SettingDetail(header: String, content: @Composable (ColumnScope.() -> Unit)) =
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(SETTINGS_HEADER_SIZE).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(header, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.onSurface)
        }
        VerticallyScrollableArea { scrollState ->
            LazyColumn(state = scrollState) {
                item {
                    content()
                }
            }
        }

}

@Composable
fun GroupDetailItem(
    label: String,
    description: String,
    selectable_text: Boolean = false,
) = Box(modifier = Modifier.padding(start = 15.dp, end = 22.dp, top = 5.dp, bottom = 2.dp)) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Row(Modifier.fillMaxWidth()
                .height(SETTINGS_HEADER_SIZE)
                .padding(horizontal = 16.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = description
                }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                if (selectable_text)
                {
                    SelectionContainer(modifier = Modifier.padding(all = 0.dp))
                    {
                        Text(label)
                    }
                } else {
                    Text(label)
                }
            }
        }
}
