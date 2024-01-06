package org.dianqk.ruslin.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import org.dianqk.ruslin.ui.theme.RuslinTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RuslinApp() {
    RuslinTheme {
        val navController = rememberNavController()
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
