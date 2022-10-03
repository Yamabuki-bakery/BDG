package org.yamabuki.bdgallery.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.ui.theme.myColors

// 簡單實現一下底部導航欄以供調用

@Composable
fun BangNavBar(
    allScreens: List<BangAppScreen>,
    currentScreen: BangAppScreen,
    onTabSelected: (BangAppScreen) -> Unit,
    showHide: Boolean,
    height: Dp = 64.dp
) {
    val density = LocalDensity.current
    val totalHeight: Dp = height + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val offset: Float by animateFloatAsState(
        if (showHide) 0f
        else with(density) { totalHeight.toPx() }
    )


    Surface(
        color = MaterialTheme.myColors.surface,
        elevation = 8.0.dp,
        modifier = Modifier.height(totalHeight)
            .graphicsLayer { translationY = offset }

        // 我不太清楚這裏應該用 offset 還是 graphicsLayer

        // 不把這個設成和 Navigation Bar 一樣的話，顔色就會有微妙的差異，我 tm
        // 我日，設完也有微妙的差異，直接把下面那個設成透明，我佛了
        //shadowElevation = 15.0.dp
    ) {
        BottomNavigation(
            //modifier = Modifier.windowInsetsPadding(
            //    // 這個 modifier 是從 nowinandroid 抄來的，能直接計算系統欄的高度，感恩
            //    WindowInsets.navigationBars.only( // 或者 safe drawing？
            //        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            //    ),
            //),
            modifier = Modifier
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                .fillMaxSize(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
        ) {
            allScreens.forEachIndexed { index, screen ->
                BottomNavigationItem(
                    //icon = { Icon(imageVector = screen.icon, contentDescription = screen.name) },
                    icon = {
                        Icon(
                            painter = painterResource(id = screen.icon),
                            contentDescription = screen.name
                        )
                    },
                    label = { Text(text = screen.name) },
                    selected = currentScreen == screen,
                    onClick = { onTabSelected(screen) },
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BangNavBarPreview() {
    BangNavBar(
        allScreens = BangAppScreen.values().toList(),
        currentScreen = BangAppScreen.Home,
        onTabSelected = {},
        true
    )
}

private val TabHeight = 56.dp
private const val InactiveTabOpacity = 0.60f

private const val TabFadeInAnimationDuration = 150
private const val TabFadeInAnimationDelay = 100
private const val TabFadeOutAnimationDuration = 100