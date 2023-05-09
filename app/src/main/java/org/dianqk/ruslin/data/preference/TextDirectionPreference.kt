package org.dianqk.ruslin.data.preference

import android.content.Context
import androidx.compose.ui.text.style.TextDirection
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.DataStoreKeys
import org.dianqk.ruslin.data.dataStore

sealed class TextDirectionPreference(val value: Int) {
    object Ltr : TextDirectionPreference(1)
    object Rtl : TextDirectionPreference(2)
    object Auto : TextDirectionPreference(3)

    fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.edit {
                it[DataStoreKeys.TextDirection.key] = value
            }
        }
    }

    fun toDesc(context: Context): String = when (this) {
        Ltr -> context.getString(R.string.ltr)
        Rtl -> context.getString(R.string.rtl)
        Auto -> context.getString(R.string.auto)
    }

    fun toHtmlDirAttribute(): String = when (this) {
        Ltr -> "ltr"
        Rtl -> "rtl"
        Auto -> "auto"
    }

    fun getTextDirection(): TextDirection = when (this) {
        Ltr -> TextDirection.Ltr
        Rtl -> TextDirection.Rtl
        Auto -> TextDirection.Content
    }

    companion object {
        val default = Ltr
        val values = listOf(
            Ltr,
            Rtl,
            Auto
        )

        fun fromPreferences(preferences: Preferences): TextDirectionPreference =
            when (preferences[DataStoreKeys.TextDirection.key]) {
                1 -> Ltr
                2 -> Rtl
                3 -> Auto
                else -> default
            }
    }
}
