package org.briarproject.briar.desktop.ui

import SnackBarToast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.zoffcc.applications.trifa.HelperFriend
import com.zoffcc.applications.trifa.HelperGeneric
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_add
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_public_key
import com.zoffcc.applications.trifa.TRIFAGlobals.UINT32_MAX_JAVA
import com.zoffcc.applications.trifa.ToxVars.TOX_ADDRESS_SIZE
import contactstore
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun AddFriend() = Box {
    var add_friend_toxid by remember { mutableStateOf("") }
    var add_button_enabled by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(30.dp))
        TextField(singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(0.dp).width(400.dp),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color(ChatColorsConfig.LIGHT__TEXTFIELD_BGCOLOR)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
            ), value = add_friend_toxid, placeholder = {
                Text(i18n("ui.addfriend.add_friend_toxid"), fontSize = 13.sp)
            }, onValueChange = {
                add_friend_toxid = it
                if (it.length == (TOX_ADDRESS_SIZE * 2))
                {
                    // HINT: correct toxid hex length
                    add_button_enabled = true
                } else
                {
                    add_button_enabled = false
                }
            })
        Spacer(modifier = Modifier.width(30.dp))
        Button(
            onClick = {
                // HINT: invite Friend
                val friendnum: Long = tox_friend_add(add_friend_toxid, i18n("ui.addfriend.add_me"))
                if (friendnum > -1)
                {
                    if (friendnum != UINT32_MAX_JAVA)
                    {
                        // HINT: friend added ok
                        add_friend_toxid = ""
                        add_button_enabled = false
                        HelperGeneric.update_savedata_file_wrapper()
                        try
                        {
                            val friend_pubkey = tox_friend_get_public_key(friendnum)
                            try
                            {
                                HelperFriend.add_friend_real(friend_pubkey)
                            }
                            catch(_: Exception)
                            {
                            }
                            contactstore.add(item = ContactItem(name = i18n("ui.addfriend.new_friend") + " #" + friendnum,
                                isConnected = 0,
                                pubkey = friend_pubkey!!,
                                push_url = "",
                                is_relay = false))
                        } catch (_: Exception)
                        {
                        }
                        SnackBarToast(i18n("ui.addfriend.invited_Friend"))
                    } else
                    {
                        // some error on adding friend
                    }
                } else
                {
                    // some error on adding friend
                }
            }, colors = ButtonDefaults.buttonColors(), enabled = add_button_enabled) {
            Text(i18n("ui.addfriend.invite_friend"))
        }
    }
}
