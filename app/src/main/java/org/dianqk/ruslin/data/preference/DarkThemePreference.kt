package org.dianqk.ruslin.data.preference

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.DataStoreKeys
import org.dianqk.ruslin.data.dataStore

sealed class DarkThemePreference(val value: Int) {
    object UseDeviceTheme : DarkThemePreference(0)
    object ON : DarkThemePreference(1)
    object OFF : DarkThemePreference(2)

    fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.edit {
                it[DataStoreKeys.DarkTheme.key] = value
            }
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            UseDeviceTheme -> context.getString(R.string.use_device_theme)
            ON -> context.getString(R.string.on)
            OFF -> context.getString(R.string.off)
        }

    @Composable
    @ReadOnlyComposable
    fun isDarkTheme(): Boolean = when (this) {
        UseDeviceTheme -> isSystemInDarkTheme()
        ON -> true
        OFF -> false
    }

    companion object {

        val default = UseDeviceTheme
        val values = listOf(UseDeviceTheme, ON, OFF)

        fun fromInt(i: Int?) = when (i) {
            0 -> UseDeviceTheme
            1 -> ON
            2 -> OFF
            else -> default
        }

        fun fromPreferences(preferences: Preferences) =
            fromInt(preferences[DataStoreKeys.DarkTheme.key])
    }
}

@Composable
operator fun DarkThemePreference.not(): DarkThemePreference =
    when (this) {
        DarkThemePreference.UseDeviceTheme -> if (isSystemInDarkTheme()) {
            DarkThemePreference.OFF
        } else {
            DarkThemePreference.ON
        }

        DarkThemePreference.ON -> DarkThemePreference.OFF
        DarkThemePreference.OFF -> DarkThemePreference.ON
    }