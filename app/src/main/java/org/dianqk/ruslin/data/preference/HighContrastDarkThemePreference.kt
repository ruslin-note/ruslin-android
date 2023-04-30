package org.dianqk.ruslin.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.DataStoreKeys
import org.dianqk.ruslin.data.dataStore

sealed class HighContrastDarkThemePreference(val value: Boolean) {
    object ON : HighContrastDarkThemePreference(true)
    object OFF : HighContrastDarkThemePreference(false)

    fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.edit {
                it[DataStoreKeys.HighContrastDarkTheme.key] = value
            }
        }
    }

    companion object {

        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKeys.HighContrastDarkTheme.key]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun HighContrastDarkThemePreference.not(): HighContrastDarkThemePreference =
    when (value) {
        true -> HighContrastDarkThemePreference.OFF
        false -> HighContrastDarkThemePreference.ON
    }