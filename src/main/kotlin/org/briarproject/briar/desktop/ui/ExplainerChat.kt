package org.briarproject.briar.desktop.ui

import Theme
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.briarproject.briar.desktop.contact.ContactItem
import org.briarproject.briar.desktop.utils.InternationalizationUtils.i18n

@Composable
fun ExplainerChat() {
    Theme {
        Explainer(headline = i18n("ui.no_contacts_selected"), text = i18n("ui.select_contact_to_start_chatting"))
    }
}

@Composable
fun ExplainerInfoIsRelay(selectedContact: ContactItem?)
{
    Theme {
        Explainer(headline = i18n("ui.tox_proxy_selected"), text = i18n("ui.explain_what_toxproxy_is"))
    }
}

val PARAGRAPH_WIDTH = 540.dp

@Composable
fun ExplainerToxNotRunning() =
Explainer(headline = i18n("ui.tox_is_not_running"), text = i18n("ui.press_the_start_button"))
{}


@Composable
fun Explainer(headline: String, text: String, content: @Composable () -> Unit = {}) =
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // BriarLogo(modifier = Modifier.size(200.dp))
        Text(
            text = headline,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            style = MaterialTheme.typography.h4
        )
        Text(
            text = text,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp).widthIn(max = PARAGRAPH_WIDTH),
            style = MaterialTheme.typography.body2.copy(textAlign = TextAlign.Center)
        )
        content()
    }
