package org.dianqk.ruslin.ui.page.settings

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.dianqk.ruslin.BuildConfig
import org.dianqk.ruslin.R
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.SettingItem
import org.dianqk.ruslin.ui.ext.showToast

private const val REPO_URL = "https://github.com/ruslin-note/ruslin-android"
private const val RELEASE_URL = "https://github.com/ruslin-note/ruslin-android/releases"
private const val GITHUB_NEW_ISSUE_URL =
    "https://github.com/ruslin-note/ruslin-android/issues/new/choose"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    navigateToCredits: () -> Unit,
    onPopBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val fraction =
        CubicBezierEasing(1f, 0f, 0.8f, 0.4f).transform(scrollBehavior.state.overlappedFraction)
    val context = LocalContext.current
    val version = "${BuildConfig.VERSION_NAME}-${BuildConfig.COMMIT_HASH}-${BuildConfig.BUILD_TYPE}"
    val clipboardManager = LocalClipboardManager.current
    val infoCopied = stringResource(id = R.string.info_copied)
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.about),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = fraction)
                    )
                },
                navigationIcon = { BackButton(onClick = onPopBack) },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = stringResource(id = R.string.about),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.readme),
                    description = stringResource(id = R.string.readme_desc),
                    icon = Icons.Filled.Description,
                    onClick = {
                        uriHandler.openUri(REPO_URL)
                    }
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.release),
                    description = stringResource(id = R.string.release_desc),
                    icon = Icons.Filled.NewReleases,
                    onClick = {
                        uriHandler.openUri(RELEASE_URL)
                    }
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.github_issue),
                    description = stringResource(id = R.string.github_issue_desc),
                    icon = Icons.Filled.BugReport,
                    onClick = {
                        uriHandler.openUri(GITHUB_NEW_ISSUE_URL)
                    }
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.credits),
                    description = stringResource(id = R.string.credits_desc),
                    icon = Icons.Filled.AutoAwesome,
                    onClick = { navigateToCredits() }
                )
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.version),
                    description = version,
                    icon = Icons.Filled.Info,
                    onClick = {
                        val info =
                            "App version: $version\nDevice information: ${Build.DEVICE} (API ${Build.VERSION.SDK_INT})"
                        clipboardManager.setText(AnnotatedString(info))
                        context.showToast(infoCopied)
                    }
                )
            }
        }
    }
}
