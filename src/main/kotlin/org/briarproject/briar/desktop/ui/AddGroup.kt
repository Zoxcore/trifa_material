package org.briarproject.briar.desktop.ui

import SnackBarToast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.hex_to_bytes
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_join
import com.zoffcc.applications.trifa.RandomNameGenerator
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_CHAT_ID_SIZE
import groupstore
import org.briarproject.briar.desktop.contact.GroupItem
import java.nio.ByteBuffer
import java.util.*

@Composable
fun AddGroup() = Box {
    var join_group_id by remember { mutableStateOf("") }
    var join_group_button_enabled by remember { mutableStateOf(false) }
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(30.dp))
            TextField(singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(0.dp).width(400.dp),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                ), value = join_group_id, placeholder = {
                    Text("enter GroupID to join ...", fontSize = 13.sp)
                }, onValueChange = {
                    join_group_id = it
                    if (it.length == (ToxVars.TOX_GROUP_CHAT_ID_SIZE * 2))
                    {
                        // HINT: correct group id hex length
                        join_group_button_enabled = true
                    } else
                    {
                        join_group_button_enabled = false
                    }
                })
            Spacer(modifier = Modifier.width(30.dp))
            Button(
                onClick = {
                    // HINT: join Group
                    val join_chat_id_buffer: ByteBuffer = ByteBuffer.allocateDirect(TOX_GROUP_CHAT_ID_SIZE)
                    val data_join: ByteArray = hex_to_bytes(join_group_id.uppercase())
                    join_chat_id_buffer.put(data_join)
                    join_chat_id_buffer.rewind()
                    val new_group_num = tox_group_join(join_chat_id_buffer, TOX_GROUP_CHAT_ID_SIZE.toLong(),
                        RandomNameGenerator.getFullName(Random()), null)

                    update_savedata_file_wrapper()

                    if ((new_group_num >= 0) && (new_group_num < UINT32_MAX_JAVA))
                    {
                        // HINT: joined Group
                        join_group_id = ""
                        join_group_button_enabled = false
                        try
                        {
                            val new_privacy_state = MainActivity.tox_group_get_privacy_state(new_group_num)
                            val group_name = "Group #" + new_group_num
                            val group_num_peers = MainActivity.tox_group_peer_count(new_group_num)
                            groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(),
                                name = group_name!!, isConnected = 0,
                                groupId = join_group_id.lowercase(),
                                privacyState = new_privacy_state))
                        } catch (_: Exception)
                        {
                        }
                        SnackBarToast("You joined new Group")
                    } else
                    {
                        // some error on joining group
                    }
                }, colors = ButtonDefaults.buttonColors(), enabled = join_group_button_enabled) {
                Text("join Group")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Spacer(modifier = Modifier.width(50.dp))
            Text("or")
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Spacer(modifier = Modifier.width(30.dp))
            Button(
                onClick = {
                    // HINT: join Group
                    val join_chat_id_buffer: ByteBuffer = ByteBuffer.allocateDirect(TOX_GROUP_CHAT_ID_SIZE)
                    val data_join: ByteArray = hex_to_bytes(TRIFAGlobals.TOX_TRIFA_PUBLIC_GROUPID.uppercase())
                    join_chat_id_buffer.put(data_join)
                    join_chat_id_buffer.rewind()
                    val new_group_num = tox_group_join(join_chat_id_buffer, TOX_GROUP_CHAT_ID_SIZE.toLong(),
                        RandomNameGenerator.getFullName(Random()), null)

                    update_savedata_file_wrapper()

                    if ((new_group_num >= 0) && (new_group_num < UINT32_MAX_JAVA))
                    {
                        // HINT: joined Group
                        try
                        {
                            val new_privacy_state = MainActivity.tox_group_get_privacy_state(new_group_num)
                            val group_name = "TRIfA Info Group"
                            val group_num_peers = MainActivity.tox_group_peer_count(new_group_num)
                            groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(),
                                name = group_name!!, isConnected = 0,
                                groupId = TRIFAGlobals.TOX_TRIFA_PUBLIC_GROUPID.lowercase(),
                                privacyState = new_privacy_state))
                        } catch (_: Exception)
                        {
                        }
                        SnackBarToast("You joined the Public Information Group")
                    } else
                    {
                        // some error on joining group
                    }
                }, colors = ButtonDefaults.buttonColors()) {
                Text("join the Public Information Group")
            }
        }
    }
}

