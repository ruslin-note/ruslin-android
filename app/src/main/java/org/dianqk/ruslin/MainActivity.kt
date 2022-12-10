package org.dianqk.ruslin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import org.dianqk.ruslin.ui.RuslinApp
import uniffi.ruslin.RuslinAndroidData

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ruslinData = RuslinAndroidData("/test/dir")
        ruslinData.simpleLog("Hello")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RuslinApp()
        }
    }
}
