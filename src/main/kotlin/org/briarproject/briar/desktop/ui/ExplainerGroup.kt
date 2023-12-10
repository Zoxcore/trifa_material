package org.briarproject.briar.desktop.ui

import Theme
import androidx.compose.runtime.Composable
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun ExplainerGroup()
{
    Theme {
        Explainer(headline = i18n("ui.no_groups_selected"), text = i18n("ui.select_group_to_start_chatting"))
    }
}
