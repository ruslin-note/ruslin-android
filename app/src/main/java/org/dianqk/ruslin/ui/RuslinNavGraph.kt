package org.dianqk.ruslin.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.FOLDER_ID_ARG
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.NOTE_ID_ARG
import org.dianqk.ruslin.ui.ext.animatedComposable
import org.dianqk.ruslin.ui.page.login.LoginPage
import org.dianqk.ruslin.ui.page.note_detail.NoteDetailPage
import org.dianqk.ruslin.ui.page.notes.NotesPage
import org.dianqk.ruslin.ui.page.preview.NotePreviewPage
import org.dianqk.ruslin.ui.page.search.SearchPage
import org.dianqk.ruslin.ui.page.settings.SettingsPage
import org.dianqk.ruslin.ui.page.settings.accounts.AccountDetailPage
import org.dianqk.ruslin.ui.page.settings.tools.ToolsPage
import org.dianqk.ruslin.ui.page.settings.tools.database.DatabaseStatusPage
import org.dianqk.ruslin.ui.page.settings.tools.log.LogPage

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
                },
                navigateToLogin = {
                    navigationActions.navigateToLogin()
                },
                navigateToSettings = {
                    navigationActions.navigateToSettings()
                },
                navigateToSearch = {
                    navigationActions.navigateToSearch()
                }
            )
        }
        animatedComposable(
            RuslinDestinations.NOTE_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(NOTE_ID_ARG) { type = NavType.StringType; nullable = true },
                navArgument(FOLDER_ID_ARG) { type = NavType.StringType; nullable = true },
            )
        ) {
            NoteDetailPage(
                navigationPreview = {
                    navigationActions.navigateToPreview(it)
                },
                onPopBack = {
                    navController.popBackStack()
                }
            )
        }
        animatedComposable(RuslinDestinations.LOGIN_ROUTE) {
            LoginPage(
                onLoginSuccess = {
                    navController.popBackStack()
                },
                onPopBack = {
                    navController.popBackStack()
                }
            )
        }
        animatedComposable(RuslinDestinations.SETTINGS_ROUTE) {
            SettingsPage(
                navigateToAccountDetail = {
                    navigationActions.navigateToAccountDetail()
                },
                navigateToTools = {
                    navigationActions.navigateToTools()
                },
                onPopBack = {
                    navController.popBackStack()
                }
            )
        }
        animatedComposable(RuslinDestinations.ACCOUNT_DETAIL_ROUTE) {
            AccountDetailPage(
                navigateToLogin = {
                    navigationActions.navigateToLogin()
                },
                onPopBack = {
                    navController.popBackStack()
                }
            )
        }
        animatedComposable(RuslinDestinations.TOOLS_ROUTE) {
            ToolsPage(
                navigateToLogDetail = { navigationActions.navigateToLog() },
                navigateToDatabaseStatus = { navigationActions.navigateToDatabaseStatus() }
            ) {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.LOG_ROUTE) {
            LogPage {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.DATABASE_STATUS_ROUTE) {
            DatabaseStatusPage {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.SEARCH_ROUTE) {
            SearchPage(navigateToNoteDetail = { noteId ->
                navigationActions.navigateToNoteDetail(null, noteId)
            }) {
                navController.popBackStack()
            }
        }
        animatedComposable(
            RuslinDestinations.PREVIEW_ROUTE,
            arguments = listOf(
                navArgument(NOTE_ID_ARG) { type = NavType.StringType; nullable = false },
            )
        ) {
            NotePreviewPage(onPopBack = {
                navController.popBackStack()
            }, navigateToNote = {
                navigationActions.navigateToNote(it)
            })
        }
    }
}
