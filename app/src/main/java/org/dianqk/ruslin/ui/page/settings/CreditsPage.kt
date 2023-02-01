package org.dianqk.ruslin.ui.page.settings

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton

data class Credit(val title: String, val license: String, val url: String)

const val GPL_V3 = "GNU General Public License v3.0"

//const val GPL_V2 = "GNU General Public License v2.0"
const val AGPL_V3 = "GNU Affero General Public License v3.0"
const val APACHE_V2 = "Apache License, Version 2.0"
const val MOZILLA_V2 = "Mozilla Public License 2.0"
const val MIT = "MIT License"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsPage(onPopBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val uriHandler = LocalUriHandler.current

    val creditsList = listOf(
        Credit("Android Jetpack", APACHE_V2, "https://github.com/androidx/androidx"),
        Credit("Kotlin", APACHE_V2, "https://kotlinlang.org/"),
        Credit("Read You", GPL_V3, "https://github.com/Ashinch/ReadYou"),
        Credit("Seal", GPL_V3, "https://github.com/JunkFood02/Seal"),
        Credit("Accompanist", APACHE_V2, "https://github.com/google/accompanist"),
        Credit("Material Design 3", APACHE_V2, "https://m3.material.io/"),
        Credit("Material Icons", APACHE_V2, "https://fonts.google.com/icons"),
        Credit("Joplin", AGPL_V3, "https://github.com/laurent22/joplin"),
        Credit("Rust", APACHE_V2, "https://github.com/rust-lang/rust"),
        Credit("uniffi-rs", MOZILLA_V2, "https://github.com/mozilla/uniffi-rs"),
        Credit("pulldown-cmark", MIT, "https://github.com/raphlinus/pulldown-cmark"),
        Credit("jieba-rs", MIT, "https://github.com/messense/jieba-rs"),
        Credit("github-markdown-css", MIT, "https://github.com/sindresorhus/github-markdown-css"),
    ).sortedBy { it.title }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.credits),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = fraction)
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = stringResource(id = R.string.credits),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            items(items = creditsList, key = { it.title }) { item: Credit ->
                CreditItem(
                    title = item.title,
                    license = item.license,
                    onClick = {
                        uriHandler.openUri(item.url)
                    }
                )
            }
        }
    }
}

@Composable
fun CreditItem(
    title: String,
    license: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                with(MaterialTheme) {
                    Text(
                        text = title,
                        maxLines = 1,
                        style = typography.titleMedium,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = license,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = typography.bodyMedium,
                    )
                }
            }
        }
    }

}