package org.dianqk.ruslin.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.dianqk.ruslin.ui.ext.animatedComposable
import org.dianqk.ruslin.ui.page.note_detail.NoteDetailPage
import org.dianqk.ruslin.ui.page.notes.NotesPage

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RuslinNavGraph(
    navController: NavHostController = rememberAnimatedNavController(),
    navigationActions: RuslinNavigationActions = remember(navController) {
        RuslinNavigationActions(navController)
    },
    startDestination: String = RuslinDestinations.NOTES_ROUTE
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        animatedComposable(RuslinDestinations.NOTES_ROUTE) {
            NotesPage(
                navigateToNoteDetail = { noteId ->
                    navigationActions.navigateToNoteDetail()
                }
            )
        }
        animatedComposable(RuslinDestinations.NOTE_DETAIL_ROUTE) {
            NoteDetailPage()
        }
    }
}