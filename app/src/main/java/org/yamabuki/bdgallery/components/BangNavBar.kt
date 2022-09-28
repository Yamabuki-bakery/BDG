package org.yamabuki.bdgallery.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
) {
    //val density = LocalDensity.current

    AnimatedVisibility(
        visible = showHide,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 50,
                easing = LinearOutSlowInEasing
            ),
            initialOffsetY = { it * 2 }
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 50,
                easing = FastOutLinearInEasing
            ),
            targetOffsetY = { it * 2 }
        )
    ) {
        Surface(
            color = MaterialTheme.myColors.surface,
            elevation = 5.0.dp,
            // 不把這個設成和 Navigation Bar 一樣的話，顔色就會有微妙的差異，我 tm
            // 我日，設完也有微妙的差異，直接把下面那個設成透明，我佛了
            //shadowElevation = 15.0.dp
        ) {
            BottomNavigation(
                modifier = Modifier.windowInsetsPadding(
                    // 這個 modifier 是從 nowinandroid 抄來的，能直接計算系統欄的高度，感恩
                    WindowInsets.navigationBars.only( // 或者 safe drawing？
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    ),
                ),
                backgroundColor = Color.Transparent
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