package org.dianqk.ruslin.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.dianqk.ruslin.ui.theme.RuslinTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RuslinApp(
) {
    RuslinTheme {
        val navController = rememberAnimatedNavController();
        val navigationActions = remember(navController) {
            RuslinNavigationActions(navController)
        }
        RuslinNavGraph(
            navController = navController,
            navigationActions = navigationActions
        )
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RuslinTheme {
        Greeting("Android")
    }
}