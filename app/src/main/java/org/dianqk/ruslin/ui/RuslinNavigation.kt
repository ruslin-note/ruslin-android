package org.dianqk.ruslin.ui

import android.net.Uri
import android.util.Log
import androidx.navigation.NavHostController
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.FOLDER_ID_ARG
import org.dianqk.ruslin.ui.RuslinDestinationsArgs.NOTE_ID_ARG
import org.dianqk.ruslin.ui.RuslinPages.NOTES_PAGE
import org.dianqk.ruslin.ui.RuslinPages.NOTE_DETAIL_PAGE

object RuslinPages {
    const val NOTES_PAGE = "notes"
    const val NOTE_DETAIL_PAGE = "note_detail"
}

object RuslinDestinationsArgs {
    const val NOTE_ID_ARG = "noteId"
    const val FOLDER_ID_ARG = "folderId"
}

object RuslinDestinations {
    const val NOTES_ROUTE = NOTES_PAGE
    const val NOTE_DETAIL_ROUTE = "$NOTE_DETAIL_PAGE?$NOTE_ID_ARG={$NOTE_ID_ARG}&$FOLDER_ID_ARG={$FOLDER_ID_ARG}"
}

class RuslinNavigationActions(private val navController: NavHostController) {
    fun navigateToNotes() {
        navController.navigate(RuslinDestinations.NOTES_ROUTE)
    }

    fun navigateToNoteDetail(folderId: String?, noteId: String?) {
        val builder = Uri.Builder()
        builder.path(NOTE_DETAIL_PAGE)
        if (folderId != null) {
            builder.appendQueryParameter(FOLDER_ID_ARG, folderId)
        }
        if (noteId != null) {
            builder.appendQueryParameter(NOTE_ID_ARG, noteId)
        }
        val url = builder.build().toString()
        navController.navigate(url)
    }
}
