package org.dianqk.ruslin.ui

import android.net.Uri
import androidx.navigation.NavHostController
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.FOLDER_ID_ARG
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.IS_PREVIEW_ARG
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.NOTE_ID_ARG
import org.dianqk.ruslin.ui.RuslinPages.ABOUT_PAGE
import org.dianqk.ruslin.ui.RuslinPages.ACCOUNT_DETAIL_PAGE
import org.dianqk.ruslin.ui.RuslinPages.APPEARANCE_PAGE
import org.dianqk.ruslin.ui.RuslinPages.CREDITS_PAGE
import org.dianqk.ruslin.ui.RuslinPages.DARK_THEME_PAGE
import org.dianqk.ruslin.ui.RuslinPages.DATABASE_STATUS_PAGE
import org.dianqk.ruslin.ui.RuslinPages.LANGUAGES_PAGE
import org.dianqk.ruslin.ui.RuslinPages.LOGIN_PAGE
import org.dianqk.ruslin.ui.RuslinPages.LOG_PAGE
import org.dianqk.ruslin.ui.RuslinPages.NOTES_PAGE
import org.dianqk.ruslin.ui.RuslinPages.NOTE_DETAIL_PAGE
import org.dianqk.ruslin.ui.RuslinPages.SEARCH_PAGE
import org.dianqk.ruslin.ui.RuslinPages.SETTINGS_PAGE
import org.dianqk.ruslin.ui.RuslinPages.TOOLS_PAGE

object RuslinPages {
    const val NOTES_PAGE = "notes"
    const val NOTE_DETAIL_PAGE = "note_detail"
    const val LOGIN_PAGE = "login"
    const val SETTINGS_PAGE = "settings"
    const val ACCOUNT_DETAIL_PAGE = "account_detail"
    const val TOOLS_PAGE = "tools"
    const val LOG_PAGE = "log"
    const val DATABASE_STATUS_PAGE = "database_status"
    const val SEARCH_PAGE = "search"
    const val ABOUT_PAGE = "about"
    const val CREDITS_PAGE = "credits"
    const val LANGUAGES_PAGE = "languages"
    const val APPEARANCE_PAGE = "appearance"
    const val DARK_THEME_PAGE = "dark_theme"
}

object RuslinDestinationsArgs {
    const val NOTE_ID_ARG = "noteId"
    const val FOLDER_ID_ARG = "folderId"
    const val IS_PREVIEW_ARG = "isPreview"
}

object RuslinDestinations {
    const val NOTES_ROUTE = NOTES_PAGE
    const val NOTE_DETAIL_ROUTE =
        "$NOTE_DETAIL_PAGE?$NOTE_ID_ARG={$NOTE_ID_ARG}&$FOLDER_ID_ARG={$FOLDER_ID_ARG}&$IS_PREVIEW_ARG={$IS_PREVIEW_ARG}"
    const val LOGIN_ROUTE = LOGIN_PAGE
    const val SETTINGS_ROUTE = SETTINGS_PAGE
    const val ACCOUNT_DETAIL_ROUTE = ACCOUNT_DETAIL_PAGE
    const val TOOLS_ROUTE = TOOLS_PAGE
    const val LOG_ROUTE = LOG_PAGE
    const val DATABASE_STATUS_ROUTE = DATABASE_STATUS_PAGE
    const val SEARCH_ROUTE = SEARCH_PAGE
    const val ABOUT_ROUTE = ABOUT_PAGE
    const val CREDITS_ROUTE = CREDITS_PAGE
    const val LANGUAGES_ROUTE = LANGUAGES_PAGE
    const val APPEARANCE_ROUTE = APPEARANCE_PAGE
    const val DARK_THEME_ROUTE = DARK_THEME_PAGE
}

class RuslinNavigationActions(private val navController: NavHostController) {
    fun navigateToNote(noteId: String, isPreview: Boolean = true) {
        val builder = Uri.Builder()
        builder.path(NOTE_DETAIL_PAGE)
        builder.appendQueryParameter(NOTE_ID_ARG, noteId)
        builder.appendQueryParameter(IS_PREVIEW_ARG, isPreview.toString())
        val url = builder.build().toString()
        navController.navigate(url)
    }

    fun navigateToNewNote(folderId: String?) {
        val builder = Uri.Builder()
        builder.path(NOTE_DETAIL_PAGE)
        if (folderId != null) {
            builder.appendQueryParameter(FOLDER_ID_ARG, folderId)
        }
        val url = builder.build().toString()
        navController.navigate(url) {
            launchSingleTop = true
        }
    }

    fun navigateToLogin() {
        navController.navigate(RuslinDestinations.LOGIN_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToSettings() {
        navController.navigate(RuslinDestinations.SETTINGS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToAccountDetail() {
        navController.navigate(RuslinDestinations.ACCOUNT_DETAIL_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToTools() {
        navController.navigate(RuslinDestinations.TOOLS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToLog() {
        navController.navigate(RuslinDestinations.LOG_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToDatabaseStatus() {
        navController.navigate(RuslinDestinations.DATABASE_STATUS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToSearch() {
        navController.navigate(RuslinDestinations.SEARCH_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToAbout() {
        navController.navigate(RuslinDestinations.ABOUT_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToCredits() {
        navController.navigate(RuslinDestinations.CREDITS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToLanguages() {
        navController.navigate(RuslinDestinations.LANGUAGES_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToAppearance() {
        navController.navigate(RuslinDestinations.APPEARANCE_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToDarkTheme() {
        navController.navigate(RuslinDestinations.DARK_THEME_ROUTE) {
            launchSingleTop = true
        }
    }
}
