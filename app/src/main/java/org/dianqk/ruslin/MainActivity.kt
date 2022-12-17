package org.dianqk.ruslin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import org.dianqk.ruslin.ui.RuslinApp
import uniffi.ruslin.RuslinAndroidData

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var ruslinAndroidData: RuslinAndroidData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RuslinApp()
        }
    }
}
