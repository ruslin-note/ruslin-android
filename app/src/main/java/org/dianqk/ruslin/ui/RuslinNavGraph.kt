package org.dianqk.ruslin.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.FOLDER_ID_ARG
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.IS_PREVIEW_ARG
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.NOTE_ID_ARG
import org.dianqk.ruslin.ui.ext.animatedComposable
import org.dianqk.ruslin.ui.page.login.LoginPage
import org.dianqk.ruslin.ui.page.note_detail.NoteDetailPage
import org.dianqk.ruslin.ui.page.notes.NotesPage
import org.dianqk.ruslin.ui.page.search.SearchPage
import org.dianqk.ruslin.ui.page.settings.*
import org.dianqk.ruslin.ui.page.settings.accounts.AccountDetailPage
import org.dianqk.ruslin.ui.page.settings.tools.ToolsPage
import org.dianqk.ruslin.ui.page.settings.tools.database.DatabaseStatusPage
import org.dianqk.ruslin.ui.page.settings.tools.log.LogPage

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RuslinNavGraph(
    navController: NavHostController = rememberNavController(),
    navigationActions: RuslinNavigationActions = remember(navController) {
        RuslinNavigationActions(navController)
    },
    startDestination: String = RuslinDestinations.NOTES_ROUTE
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        animatedComposable(RuslinDestinations.NOTES_ROUTE) {
            NotesPage(
                navigateToNote = { noteId ->
                    navigationActions.navigateToNote(noteId = noteId)
                },
                navigateToNewNote = { folderId ->
                    navigationActions.navigateToNewNote(folderId = folderId)
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
                navArgument(IS_PREVIEW_ARG) { type = NavType.BoolType; defaultValue = false },
            )
        ) {
            NoteDetailPage(
                navigateToNote = {
                    navigationActions.navigateToNote(it)
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
                navigateToAbout = {
                    navigationActions.navigateToAbout()
                },
                navigateToLanguages = {
                    navigationActions.navigateToLanguages()
                },
                navigateToAppearance = {
                    navigationActions.navigateToAppearance()
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
            SearchPage(navigateToNote = { noteId ->
                navigationActions.navigateToNote(noteId)
            }) {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.ABOUT_ROUTE) {
            AboutPage(navigateToCredits = {
                navigationActions.navigateToCredits()
            }) {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.CREDITS_ROUTE) {
            CreditsPage {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.LANGUAGES_ROUTE) {
            LanguagesPage(navigateToTextDirection = {
                navigationActions.navigateToTextDirection()
            }) {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.APPEARANCE_ROUTE) {
            AppearancePage(navigateToDarkTheme = {
                navigationActions.navigateToDarkTheme()
            }) {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.DARK_THEME_ROUTE) {
            DarkThemePage {
                navController.popBackStack()
            }
        }
        animatedComposable(RuslinDestinations.TEXT_DIRECTION_ROUTE) {
            TextDirectionPage {
                navController.popBackStack()
            }
        }
    }
}
