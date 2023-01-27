package org.dianqk.ruslin.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryTextTabs(
    modifier: Modifier = Modifier,
    titles: List<String>,
    selectedTabIndex: Int,
    onSelectedTabIndex: (Int) -> Unit,
    divider: @Composable () -> Unit = @Composable {
        Divider()
    },
) {
    // https://medium.com/@sukhdip_sandhu/jetpack-compose-scrollabletabrow-indicator-matches-width-of-text-e79c0e5826fe
    val density = LocalDensity.current
    val tabWidths = remember {
        val tabWidthStateList = mutableStateListOf<Dp>()
        repeat(titles.size) {
            tabWidthStateList.add(0.dp)
        }
        tabWidthStateList
    }
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        indicator = { tabPositions ->
            Box(
                modifier = Modifier
                    .customTabIndicatorOffset(
                        currentTabPosition = tabPositions[selectedTabIndex],
                        tabWidth = tabWidths[selectedTabIndex],
                    )
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(
                            topStart = 3.dp,
                            topEnd = 3.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
            )
        },
        divider = divider
    ) {
        titles.forEachIndexed { index, title ->
            // https://m3.material.io/components/tabs/specs
            Tab(
                modifier = Modifier.height(48.dp),
                selected = index == selectedTabIndex,
                onClick = {
                    onSelectedTabIndex(index)
                }
            ) {
                Text(
                    text = title,
                    color = if (index == selectedTabIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    onTextLayout = { textLayoutResult ->
                        tabWidths[index] =
                            with(density) { textLayoutResult.size.width.toDp() }
                    }
                )
            }
        }
    }
}

private fun Modifier.customTabIndicatorOffset(
    currentTabPosition: TabPosition,
    tabWidth: Dp
): Modifier = composed {
    composed(
        inspectorInfo = debugInspectorInfo {
            name = "customTabIndicatorOffset"
            value = currentTabPosition
        }
    ) {
        val currentTabWidth by animateDpAsState(
            targetValue = tabWidth,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
        val indicatorOffset by animateDpAsState(
            targetValue = ((currentTabPosition.left + currentTabPosition.right - tabWidth) / 2),
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
        fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart)
            .offset(x = indicatorOffset)
            .width(currentTabWidth)
    }
}
