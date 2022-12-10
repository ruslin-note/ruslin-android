package org.dianqk.ruslin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import org.dianqk.ruslin.ui.RuslinApp
import uniffi.ruslin.RuslinAndroidData

class MainActivity : ComponentActivity() {

    var ruslinAndroidData: RuslinAndroidData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ruslinAndroidData = RuslinAndroidData("/test/dir")
        ruslinAndroidData?.let {
            it.simpleLog("Hello")
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RuslinApp()
        }
    }
}
