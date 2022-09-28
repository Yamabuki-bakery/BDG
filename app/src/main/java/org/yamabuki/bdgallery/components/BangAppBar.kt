package org.yamabuki.bdgallery.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.ui.theme.myColors

@Composable
fun BangAppBar(
    currentScreen: BangAppScreen,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    //scrollBehavior: TopAppBarScrollBehavior? = null,
    statusBarTransparent: Boolean = false,
) {
    val allScreens = BangAppScreen.values().toList()
    //加入一個 Surface 來承擔頂欄的底色，并且頂欄 container 背景色已經設置為透明
    //以便系統狀態欄和頂欄的顔色一致，，，
    Surface(
        color = MaterialTheme.myColors.surface,
//        tonalElevation = 3.0.dp,
//        shadowElevation = 3.0.dp
        elevation = 3.dp
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(id = currentScreen.title),
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            },
            navigationIcon = navigationIcon,
            actions = actions,
            //scrollBehavior = scrollBehavior,
//            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                containerColor = Color.Transparent,
//            ),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun BangAppBarPreview() {
    BangAppBar(
        currentScreen = BangAppScreen.Home,
    )
}

private val TabHeight = 56.dp
private const val InactiveTabOpacity = 0.60f

private const val TabFadeInAnimationDuration = 150
private const val TabFadeInAnimationDelay = 100
private const val TabFadeOutAnimationDuration = 100