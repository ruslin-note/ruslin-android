package org.dianqk.ruslin.data.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.dianqk.ruslin.data.DataStoreKeys
import org.dianqk.ruslin.data.dataStore

object ThemeIndexPreference {

    const val default = 0

    fun put(context: Context, scope: CoroutineScope, value: Int) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.edit {
                it[DataStoreKeys.ThemeIndex.key] = value
            }
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.ThemeIndex.key] ?: default
}
