package org.briarproject.briar.desktop.ui

import SnackBarToast
import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.sorm.GroupDB
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper
import com.zoffcc.applications.trifa.HelperGroup.hex_to_bytes
import com.zoffcc.applications.trifa.MainActivity
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_get_chat_id
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_join
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_new
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_group_self_set_name
import com.zoffcc.applications.trifa.RandomNameGenerator
import com.zoffcc.applications.trifa.TRIFAGlobals
import com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA
import com.zoffcc.applications.trifa.ToxVars
import com.zoffcc.applications.trifa.ToxVars.TOX_GROUP_CHAT_ID_SIZE
import com.zoffcc.applications.trifa.TrifaToxService
import com.zoffcc.applications.trifa.TrifaToxService.Companion.orma
import groupstore
import org.briarproject.briar.desktop.contact.GroupItem
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n
import java.nio.ByteBuffer
import java.util.*

@Composable
@Preview
fun AddGroup() = Box {
    var join_group_id by remember { mutableStateOf("") }
    var join_group_button_enabled by remember { mutableStateOf(false) }
    var new_public_group_name by remember { mutableStateOf("") }
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(30.dp))
            TextField(singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(0.dp).width(400.dp),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                ), value = join_group_id, placeholder = {
                    Text(i18n("ui.group.enter_groupid"), fontSize = 13.sp)
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
                            val group_name = i18n("ui.group.new_group") + " #" + new_group_num
                            val group_num_peers = MainActivity.tox_group_peer_count(new_group_num)

                            try
                            {
                                val group_new = GroupDB()
                                group_new.group_identifier = join_group_id.lowercase()
                                group_new.privacy_state = new_privacy_state
                                group_new.name = group_name!!
                                group_new.notification_silent = false
                                orma!!.insertIntoGroupDB(group_new)
                            } catch (_: Exception)
                            {
                            }

                            groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(),
                                name = group_name!!, isConnected = 0,
                                groupId = join_group_id.lowercase(),
                                privacyState = new_privacy_state))
                        } catch (_: Exception)
                        {
                        }
                        SnackBarToast(i18n("ui.group.joined_new_group"))
                    } else
                    {
                        // some error on joining group
                        SnackBarToast(i18n("ui.group.error_joining"))
                    }
                }, colors = ButtonDefaults.buttonColors(), enabled = join_group_button_enabled) {
                Text(i18n("ui.group.join_group"))
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

                            try
                            {
                                val group_new = GroupDB()
                                group_new.group_identifier = TRIFAGlobals.TOX_TRIFA_PUBLIC_GROUPID.lowercase()
                                group_new.privacy_state = new_privacy_state
                                group_new.name = group_name!!
                                group_new.notification_silent = false
                                orma!!.insertIntoGroupDB(group_new)
                            } catch (_: Exception)
                            {
                            }

                            groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(),
                                name = group_name!!, isConnected = 0,
                                groupId = TRIFAGlobals.TOX_TRIFA_PUBLIC_GROUPID.lowercase(),
                                privacyState = new_privacy_state))
                        } catch (_: Exception)
                        {
                        }
                        SnackBarToast(i18n("ui.group.joined_the"))
                    } else
                    {
                        // some error on joining group
                        SnackBarToast(i18n("ui.group.error_joining_public"))
                    }
                }, colors = ButtonDefaults.buttonColors()) {
                Text(i18n("ui.group.join_public"))
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Spacer(modifier = Modifier.width(35.dp))
            Text(fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                text =i18n("ui.group.important_notice") + " ")
            Text(fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                text =i18n("ui.group.joining_minutes"))
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            Spacer(modifier = Modifier.width(50.dp))
            Text("or")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(30.dp))
            TextField(singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(0.dp).width(400.dp),
                colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                ), value = new_public_group_name, placeholder = {
                    Text(i18n("ui.group.enter_group_name"), fontSize = 13.sp)
                }, onValueChange = {
                    new_public_group_name = it
                })
            Spacer(modifier = Modifier.width(30.dp))
            Button(
                onClick = {
                    // HINT: create a new public Group
                    val my_peername = RandomNameGenerator.getFullName(Random())
                    val new_group_num = tox_group_new(
                        ToxVars.TOX_GROUP_PRIVACY_STATE.TOX_GROUP_PRIVACY_STATE_PUBLIC.value,
                        new_public_group_name,
                        my_peername)

                    update_savedata_file_wrapper()

                    if ((new_group_num >= 0) && (new_group_num < UINT32_MAX_JAVA))
                    {
                        // HINT: Group was created
                        var group_identifier: String = ""
                        val groupid_buf: ByteBuffer = ByteBuffer.allocateDirect(TRIFAGlobals.GROUP_ID_LENGTH * 2)
                        if (tox_group_get_chat_id(new_group_num, groupid_buf) == 0)
                        {
                            val groupid_buffer = ByteArray(TRIFAGlobals.GROUP_ID_LENGTH)
                            groupid_buf.clear()
                            groupid_buf.get(groupid_buffer, 0, TRIFAGlobals.GROUP_ID_LENGTH)
                            group_identifier = HelperGeneric.bytesToHex(groupid_buffer, 0, TRIFAGlobals.GROUP_ID_LENGTH).lowercase()
                        }

                        try
                        {
                            tox_group_self_set_name(new_group_num, my_peername)
                            update_savedata_file_wrapper()
                        } catch (_: Exception)
                        {
                        }

                        try
                        {
                            val new_privacy_state = MainActivity.tox_group_get_privacy_state(new_group_num)
                            val group_name = new_public_group_name
                            val group_num_peers = MainActivity.tox_group_peer_count(new_group_num)
                            groupstore.add(item = GroupItem(numPeers = group_num_peers.toInt(),
                                name = group_name!!, isConnected = 0,
                                groupId = group_identifier.lowercase(),
                                privacyState = new_privacy_state))
                        } catch (_: Exception)
                        {
                        }
                        new_public_group_name = ""
                        SnackBarToast(i18n("ui.group.created_new_public"))
                    } else
                    {
                        // some error on joining group
                        SnackBarToast("Error creating new public Group")
                    }
                }, colors = ButtonDefaults.buttonColors(), enabled = new_public_group_name.isNotEmpty()) {
                Text(i18n("ui.group.created_public"))
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

