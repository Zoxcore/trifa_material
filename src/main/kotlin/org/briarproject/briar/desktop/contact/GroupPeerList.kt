package org.briarproject.briar.desktop.contact

import CONTACT_COLUMN_WIDTH
import GROUP_PEER_COLUMN_WIDTH
import GROUP_PEER_HEIGHT
import TOP_HEADER_SIZE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.zoffcc.applications.trifa.StateGroupPeers
import grouppeerstore
import org.briarproject.briar.desktop.ui.ListItemView
import org.briarproject.briar.desktop.ui.VerticallyScrollableArea
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun GroupPeerList(
    grouppeerList: StateGroupPeers,
) = Column(
    modifier = Modifier.fillMaxHeight().width(GROUP_PEER_COLUMN_WIDTH).background(Color.Transparent),
) {
    VerticallyScrollableArea(modifier = Modifier.fillMaxSize()) { scrollState ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .semantics {
                    contentDescription = i18n("access.grouppeer.list")
                }
                .selectableGroup()
        ) {
            items(
                items = grouppeerList.grouppeers,
                key = { item -> item.pubkey },
                contentType = { item -> item::class }
            ) { item ->
                ListItemView(
                    onSelect = { grouppeerstore.select(item.pubkey) },
                    selected = (grouppeerList.selectedGrouppeerPubkey == item.pubkey)
                ) {
                    val modifier = Modifier
                        .heightIn(min = GROUP_PEER_HEIGHT)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(start = 16.dp, end = 4.dp)
                        GrouppeerItemView(
                            grouppeerItem = item,
                            modifier = modifier
                        )
                }
            }
        }
    }
}


