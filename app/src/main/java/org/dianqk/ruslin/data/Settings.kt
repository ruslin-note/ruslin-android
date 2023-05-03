package org.dianqk.ruslin.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.dianqk.ruslin.R
import org.dianqk.ruslin.data.preference.DarkThemePreference
import org.dianqk.ruslin.data.preference.HighContrastDarkThemePreference
import org.dianqk.ruslin.data.preference.LanguagesPreference
import org.dianqk.ruslin.data.preference.TextDirectionPreference
import org.dianqk.ruslin.data.preference.ThemeIndexPreference
import java.io.IOException
import java.util.concurrent.TimeUnit

// https://github.com/Ashinch/ReadYou/blob/435a6ea57704f45871565cb8980e1e45b69ff884/app/src/main/java/me/ash/reader/ui/ext/DataStoreExt.kt

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Context.languages: Int
    get() = this.dataStore.get(DataStoreKeys.Languages) ?: 0

sealed class DataStoreKeys<T> {

    abstract val key: Preferences.Key<T>

    object SyncInterval : DataStoreKeys<Long>() {
        override val key: Preferences.Key<Long>
            get() = longPreferencesKey("sync.syncInterval")
    }

    object SyncOnStart : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("sync.syncOnStart")
    }

    object SyncOnlyWiFi : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("sync.syncOnlyWiFi")
    }

    object SyncOnlyWhenCharging : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("sync.syncOnlyWhenCharging")
    }

    object Languages : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("languages")
    }

    object ThemeIndex : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("themeIndex")
    }

    object DarkTheme : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("darkTheme")
    }

    object HighContrastDarkTheme : DataStoreKeys<Boolean>() {
        override val key: Preferences.Key<Boolean>
            get() = booleanPreferencesKey("highContrastDarkTheme")
    }

    object TextDirection : DataStoreKeys<Int>() {
        override val key: Preferences.Key<Int>
            get() = intPreferencesKey("contentTextDirection")
    }
}

fun DataStore<Preferences>.syncStrategy(): Flow<SyncStrategy> = this.data.map {
    val default = SyncStrategy()
    SyncStrategy(
        syncInterval = it[DataStoreKeys.SyncInterval.key] ?: default.syncInterval,
        syncOnStart = it[DataStoreKeys.SyncOnStart.key] ?: default.syncOnStart,
        syncOnlyWiFi = it[DataStoreKeys.SyncOnlyWiFi.key] ?: default.syncOnlyWiFi,
        syncOnlyWhenCharging = it[DataStoreKeys.SyncOnlyWhenCharging.key]
            ?: default.syncOnlyWhenCharging
    )
}

@Suppress("UNCHECKED_CAST")
fun <T> DataStore<Preferences>.get(dataStoreKeys: DataStoreKeys<T>): T? {
    return runBlocking {
        this@get.data.catch { exception ->
            if (exception is IOException) {
                Log.e("RLog", "Get data store error $exception")
                exception.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            it[dataStoreKeys.key]
        }.first() as T
    }
}

data class SyncStrategy(
    val syncInterval: Long = SyncIntervalPreference.default.value,
    val syncOnStart: Boolean = true,
    val syncOnlyWiFi: Boolean = false,
    val syncOnlyWhenCharging: Boolean = false
)

sealed class SyncIntervalPreference(
    val value: Long
) {

    object Manually : SyncIntervalPreference(0L)
    object Every15Minutes : SyncIntervalPreference(15L)
    object Every30Minutes : SyncIntervalPreference(30L)
    object Every1Hour : SyncIntervalPreference(60L)
    object Every2Hours : SyncIntervalPreference(120L)
    object Every3Hours : SyncIntervalPreference(180L)
    object Every6Hours : SyncIntervalPreference(360L)
    object Every12Hours : SyncIntervalPreference(720L)
    object Every1Day : SyncIntervalPreference(1440L)

    fun toDesc(context: Context): String =
        when (this) {
            Manually -> context.getString(R.string.manually)
            Every15Minutes -> context.getString(R.string.every_15_minutes)
            Every30Minutes -> context.getString(R.string.every_30_minutes)
            Every1Hour -> context.getString(R.string.every_1_hour)
            Every2Hours -> context.getString(R.string.every_2_hours)
            Every3Hours -> context.getString(R.string.every_3_hours)
            Every6Hours -> context.getString(R.string.every_6_hours)
            Every12Hours -> context.getString(R.string.every_12_hours)
            Every1Day -> context.getString(R.string.every_1_day)
        }

    fun toPeriodicWorkRequestBuilder(): PeriodicWorkRequest.Builder =
        PeriodicWorkRequestBuilder<SyncWorker>(value, TimeUnit.MINUTES)

    companion object {

        fun toSyncInterval(syncInterval: Long): SyncIntervalPreference {
            return values.find { it.value == syncInterval } ?: default
        }

        val default = Every30Minutes
        val values = listOf(
            Manually,
            Every15Minutes,
            Every30Minutes,
            Every1Hour,
            Every2Hours,
            Every3Hours,
            Every6Hours,
            Every12Hours,
            Every1Day
        )
    }
}

data class Settings(
    val languages: LanguagesPreference = LanguagesPreference.default,
    val contentTextDirection: TextDirectionPreference = TextDirectionPreference.default,

    val themeIndex: Int = ThemeIndexPreference.default,
    val darkTheme: DarkThemePreference = DarkThemePreference.default,
    val highContrastDarkTheme: HighContrastDarkThemePreference = HighContrastDarkThemePreference.default,
)

private fun Preferences.toSettings(): Settings {
    return Settings(
        languages = LanguagesPreference.fromPreferences(this),
        contentTextDirection = TextDirectionPreference.fromPreferences(this),
        themeIndex = ThemeIndexPreference.fromPreferences(this),
        darkTheme = DarkThemePreference.fromPreferences(this),
        highContrastDarkTheme = HighContrastDarkThemePreference.fromPreferences(this),
    )
}

@Composable
fun SettingsProvider(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember {
        context.dataStore.data.map {
            it.toSettings()
        }
    }.collectAsState(initial = runBlocking { context.dataStore.data.first().toSettings() }).value

    CompositionLocalProvider(
        LocalLanguages provides settings.languages,
        LocalContentTextDirection provides settings.contentTextDirection,
        LocalThemeIndex provides settings.themeIndex,
        LocalDarkTheme provides settings.darkTheme,
        LocalHighContrastDarkTheme provides settings.highContrastDarkTheme,
    ) {
        content()
    }
}

val LocalLanguages = compositionLocalOf<LanguagesPreference> { LanguagesPreference.default }
val LocalContentTextDirection =
    compositionLocalOf<TextDirectionPreference> { TextDirectionPreference.default }

// Theme
val LocalThemeIndex = compositionLocalOf { ThemeIndexPreference.default }
val LocalDarkTheme = compositionLocalOf<DarkThemePreference> { DarkThemePreference.default }
val LocalHighContrastDarkTheme =
    compositionLocalOf<HighContrastDarkThemePreference> { HighContrastDarkThemePreference.default }
