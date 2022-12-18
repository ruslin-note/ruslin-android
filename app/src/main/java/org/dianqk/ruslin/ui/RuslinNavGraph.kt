package org.dianqk.ruslin.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.NOTE_ID_ARG
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
                navigateToNoteDetail = { parentId: String?, noteId: String? ->
                    navigationActions.navigateToNoteDetail(folderId = parentId, noteId = noteId)
                }
            )
        }
        animatedComposable(
            RuslinDestinations.NOTE_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(NOTE_ID_ARG) { type = NavType.StringType; nullable = true }
            )
        ) {
            NoteDetailPage(
                onPopBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}