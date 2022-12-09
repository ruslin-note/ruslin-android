package org.dianqk.ruslin.ui

import androidx.navigation.NavHostController

object RuslinDestinations {
    const val NOTES_ROUTE = "notes"
    const val NOTE_DETAIL_ROUTE = "note_detail"
}

class RuslinNavigationActions(navController: NavHostController) {
    var navigateToNotes: () -> Unit = {
        navController.navigate(RuslinDestinations.NOTES_ROUTE) {

        }
    }
    var navigateToNoteDetail: () -> Unit = {
        navController.navigate(RuslinDestinations.NOTE_DETAIL_ROUTE) {

        }
    }
}
