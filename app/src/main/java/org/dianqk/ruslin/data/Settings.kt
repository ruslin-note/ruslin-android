package org.dianqk.ruslin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dianqk.ruslin.R
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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


}

fun DataStore<Preferences>.syncStrategy(): Flow<SyncStrategy> = this.data.map {
    val default = SyncStrategy()
    SyncStrategy(
        syncInterval = it[DataStoreKeys.SyncInterval.key] ?: default.syncInterval,
        syncOnStart = it[DataStoreKeys.SyncOnStart.key] ?: default.syncOnStart,
        syncOnlyWiFi = it[DataStoreKeys.SyncOnlyWiFi.key] ?: default.syncOnlyWiFi,
        syncOnlyWhenCharging = it[DataStoreKeys.SyncOnlyWhenCharging.key]
            ?: default.syncOnlyWhenCharging,
    )
}

data class SyncStrategy(
    val syncInterval: Long = SyncIntervalPreference.default.value,
    val syncOnStart: Boolean = true,
    val syncOnlyWiFi: Boolean = false,
    val syncOnlyWhenCharging: Boolean = false,
)

sealed class SyncIntervalPreference(
    val value: Long,
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
            return values.find { it.value == syncInterval } ?: SyncIntervalPreference.default
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
            Every1Day,
        )
    }
}