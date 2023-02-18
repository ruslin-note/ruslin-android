package org.dianqk.ruslin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import org.dianqk.ruslin.data.SettingsProvider
import org.dianqk.ruslin.data.languages
import org.dianqk.ruslin.data.preference.LanguagesPreference
import org.dianqk.ruslin.ui.RuslinApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        LanguagesPreference.fromValue(languages).let {
            if (it == LanguagesPreference.UseDeviceLanguages) return@let
            it.setLocale(this)
        }

        setContent {
            SettingsProvider {
                RuslinApp()
            }
        }
    }
}
