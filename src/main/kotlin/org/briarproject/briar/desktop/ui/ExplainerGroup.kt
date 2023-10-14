package org.briarproject.briar.desktop.ui

import Theme
import androidx.compose.runtime.Composable
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun ExplainerGroup()
{
    Theme {
        Explainer(headline = i18n("No Groups selected"), text = i18n("Select a Group to start chatting"))
    }
}
