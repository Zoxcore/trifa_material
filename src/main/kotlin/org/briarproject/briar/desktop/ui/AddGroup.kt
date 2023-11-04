package org.briarproject.briar.desktop.ui

import Theme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.briarproject.briar.desktop.utils.InternationalizationUtils

@Composable
fun AddGroup() = Box {
    Theme {
        Explainer(headline = InternationalizationUtils.i18n("Not yet implemented"), text = InternationalizationUtils.i18n("..."))
    }
}

