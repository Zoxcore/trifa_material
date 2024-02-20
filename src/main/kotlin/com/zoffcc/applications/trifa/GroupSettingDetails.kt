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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_get_peer_id
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_set_name
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_self_set_name
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun GroupSettingDetails(selectedGroupId: String?)
{
    SettingDetail(i18n("ui.group_settings_headline")) {

        // ---- change own name for one-on-one chats ----
        var self_name_in_group = ""
        try
        {
            val group_num = HelperGroup.tox_group_by_groupid__wrapper(selectedGroupId)
            val self_peernum = tox_group_self_get_peer_id(group_num)
            self_name_in_group = MainActivity.tox_group_peer_get_name(group_num, self_peernum)!!
        } catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
        var tox_own_name_in_group by remember { mutableStateOf(self_name_in_group) }

        var num_messages = "?"
        try
        {
            num_messages = "" + TrifaToxService.orma!!.selectFromGroupMessage().group_identifierEq(selectedGroupId!!.lowercase()).count()
        }
        catch(_: Exception)
        {
        }
        GroupDetailItem(label = "Number of Messages: " + num_messages, description = "xy")
        Spacer(modifier = Modifier.height(5.dp))
        Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
            TextField(enabled = true, singleLine = true,
                textStyle = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(0.dp).weight(1.0f),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
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
                Text(label)
            }
        }
}
