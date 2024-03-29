package org.dianqk.ruslin.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ContentState(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(modifier = Modifier.height(20.dp))
        text()
    }
}

@Composable
fun ContentLoadingState(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
) {
    val syncAngle by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )
    ContentState(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .size(60.dp)
                    .rotate(syncAngle),
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null
            )
        },
        text = text
    )
}

@Composable
fun ContentEmptyState(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
) {
    ContentState(
        modifier = modifier,
        icon = {
            Icon(
                modifier = Modifier
                    .size(60.dp),
                imageVector = Icons.Outlined.Pending,
                contentDescription = null
            )
        },
        text = text
    )
}