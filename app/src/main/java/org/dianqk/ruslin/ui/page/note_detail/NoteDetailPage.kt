package org.dianqk.ruslin.ui.page.note_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.dianqk.ruslin.R

var body =
    "I sat down for a “virtual coffee” with Haemin Yim, founder and CEO of Creatrip, from Korea to discuss starting on the web, setting up the company for global reach, and tips for startups looking to grow.\n" +
            "In Haemin’s words “Creatrip is an app that provides a gateway to authentic Korean trends and cultures.” Last year, they took part in the ChangGoo program, an initiative that supports Korean app startups in partnership with the South Korean government. The Creatrip team applied the learnings of the program to continue to grow and now have users from over 100 countries."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailPage() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.ArrowBack, stringResource(id = R.string.desc_menu))
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.MoreVert, stringResource(id = R.string.desc_more))
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Google Play Coffee break with Creatrip | Setting up your business for global reach",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = body
            )
        }
    }
}
