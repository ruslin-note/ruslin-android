package org.dianqk.ruslin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import org.dianqk.ruslin.ui.RuslinApp
import org.dianqk.ruslin.ui.theme.RuslinTheme
import uniffi.ruslin.add

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addResult = add(1, 2);
        Log.d("RuslinApp", "1+2 $addResult")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RuslinApp()
        }
    }
}
