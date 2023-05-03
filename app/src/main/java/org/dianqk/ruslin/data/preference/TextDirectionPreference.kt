package org.dianqk.ruslin.data.preference

import android.content.Context
import androidx.compose.ui.text.style.TextDirection
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.DataStoreKeys
import org.dianqk.ruslin.data.dataStore

sealed class TextDirectionPreference(val value: Int) {
    object Ltr : TextDirectionPreference(1)
    object Rtl : TextDirectionPreference(2)
    object Content : TextDirectionPreference(3)
    object ContentOrLtr : TextDirectionPreference(4)
    object ContentOrRtl : TextDirectionPreference(5)

    fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.edit {
                it[DataStoreKeys.TextDirection.key] = value
            }
        }
    }

    fun toDesc(context: Context): String = getTextDirection().toString()

    fun getTextDirection(): TextDirection = when (this) {
        Ltr -> TextDirection.Ltr
        Rtl -> TextDirection.Rtl
        Content -> TextDirection.Content
        ContentOrLtr -> TextDirection.ContentOrLtr
        ContentOrRtl -> TextDirection.ContentOrRtl
    }

    companion object {
        val default = Ltr
        val values = listOf(
            Ltr,
            Rtl,
            Content,
            ContentOrLtr,
            ContentOrRtl,
        )

        fun fromPreferences(preferences: Preferences): TextDirectionPreference =
            when (preferences[DataStoreKeys.TextDirection.key]) {
                1 -> Ltr
                2 -> Rtl
                3 -> Content
                4 -> ContentOrLtr
                5 -> ContentOrRtl
                else -> default
            }
    }
}
