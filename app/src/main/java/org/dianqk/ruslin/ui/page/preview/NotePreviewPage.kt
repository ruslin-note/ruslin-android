package org.dianqk.ruslin.ui.page.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dianqk.ruslin.ui.component.BackButton
import org.dianqk.ruslin.ui.component.MarkdownRichText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePreviewPage(
    onPopBack: () -> Unit,
    navigateToNote: (String) -> Unit,
    viewModel: NotePreviewViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    BackButton(onClick = onPopBack)
                }
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            uiState.value.htmlText?.let {
                MarkdownRichText(htmlBodyText = it, navigateToNote = navigateToNote)
            }
        }
    }
}
