package com.zoffcc.applications.trifa

import SETTINGS_HEADER_SIZE
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoffcc.applications.sorm.FriendList
import com.zoffcc.applications.trifa.MainActivity.Companion.get_friend_ip_str
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_by_public_key
import com.zoffcc.applications.trifa.MainActivity.Companion.tox_friend_get_name
import com.zoffcc.applications.trifa.ToxVars.TOX_CAPABILITY_DECODE
import com.zoffcc.applications.trifa.ToxVars.TOX_CAPABILITY_DECODE_TO_STRING
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun FriendSettingDetails(selectedContactPubkey: String?)
{
    FriendSettingDetailV(i18n("ui.friend_settings_headline")) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(Modifier.wrapContentHeight().fillMaxWidth().padding(start = 15.dp)) {
            val f_num = tox_friend_by_public_key(selectedContactPubkey)
            var f_name: String? = ""
            try
            {
                f_name = tox_friend_get_name(f_num)
            }
            catch (_: Exception)
            {
            }
            if ((f_name == null) || (f_name.isEmpty()))
            {
                f_name = ""
            }

            var f: FriendList? = null
            try
            {
                f = HelperFriend.main_get_friend(selectedContactPubkey)
            }
            catch (_: Exception)
            {
            }

            var caps: String = "BASIC"
            if (f != null)
            {
                caps = TOX_CAPABILITY_DECODE_TO_STRING(TOX_CAPABILITY_DECODE(f.capabilities))
            }

            val t = "Name: " + f_name + "\n" +
                    "Public Key: " + selectedContactPubkey + "\n" +
                    "Capabilities: " + caps + "\n" +
                    "IP: " + get_friend_ip_str(selectedContactPubkey)
            SelectionContainer(modifier = Modifier.padding(all = 0.dp)) {
                Text(
                    text = t,
                    style = MaterialTheme.typography.subtitle1.copy(fontSize = 16.sp, lineHeight = TextUnit.Unspecified),
                    modifier = Modifier.padding(0.dp).weight(1.0f)
                )
            }
        }
    }
}

@Composable
fun FriendSettingDetailV(header: String, content: @Composable (ColumnScope.() -> Unit)) =
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
fun FriendDetailItem(
    label: String,
    description: String,
    setting: @Composable (RowScope.() -> Unit),
) = Row(Modifier.fillMaxWidth().height(SETTINGS_HEADER_SIZE).padding(horizontal = 16.dp).semantics(mergeDescendants = true) { // it would be nicer to derive the contentDescriptions from the descendants automatically
    // which is currently not supported in Compose for Desktop
    // see https://github.com/JetBrains/compose-jb/issues/2111
    contentDescription = description
}, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
    Text(label)
    setting()
}
