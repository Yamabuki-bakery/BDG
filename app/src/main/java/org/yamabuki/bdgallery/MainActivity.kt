package org.yamabuki.bdgallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowInsets.Type.navigationBars
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.navigationBars
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.yamabuki.bdgallery.components.BangNavBar
import org.yamabuki.bdgallery.screens.Favorite.FavoriteScreen
import org.yamabuki.bdgallery.screens.Gallery.GalleryScreen
import org.yamabuki.bdgallery.screens.Home.HomeScreen
import org.yamabuki.bdgallery.screens.Home.HomeViewModel
import org.yamabuki.bdgallery.screens.Stickers.StickersScreen
import org.yamabuki.bdgallery.ui.theme.BangDreamGalleryTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 設置窗口能鋪滿整個屏幕包括狀態欄和導航欄，網上抄的
        // https://google.github.io/accompanist/insets/
        // https://blog.msomu.dev/behind-status-bar-with-jetpack-compose
        // https://medium.com/mobile-app-development-publication/android-jetpack-compose-inset-padding-made-easy-5f156a790979
        // https://www.youtube.com/watch?v=_mGDMVRO3iE
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            BangApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangApp() {
    var showNavBar by rememberSaveable { mutableStateOf(true) }
    BangDreamGalleryTheme {
        //不要問這裏的代碼是怎麽來的，因爲都是抄的
        val allScreens = BangAppScreen.values().toList()
        val navController = rememberNavController()
        val backstackEntry = navController.currentBackStackEntryAsState()
        val currentScreen = BangAppScreen.fromRoute(backstackEntry.value?.destination?.route)


        //不要問 Scaffold 是什麽，我也不懂
        Scaffold(
            // 設置這個元素能鋪滿整個屏幕包括狀態欄和導航欄，網上抄的
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BangNavBar(
                    allScreens = allScreens,
                    currentScreen = currentScreen,
                    onTabSelected = { screen ->
                        if (screen != currentScreen)
                            navController.navigate(screen.name){
                                // 很神奇，加了下面幾行代碼以後，在屏幕之間導航不會丟掉已有的 viewModel
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                    },
                    showHide = showNavBar
                )
            },
        ) { innerPadding ->  // 這個值將會給出 scaffold 内以參數傳遞進去的 top app bar 和 bottom bar 的 size
            NavHost(
                navController = navController,
                startDestination = BangAppScreen.Home.name,
//                modifier = Modifier.padding(
//                    top = innerPadding.calculateTopPadding(),
//                    start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
//                    end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
//                    // bottom 不需要 padding 一個 bottom bar， 使得元素可以在 bottom bar 後面繪製
//                )
//                modifier = Modifier.windowInsetsPadding(
//                    // 因爲 top app bar 由各個 screen 所管理，所以不需要進行 top padding，
//                    // 并且，bottom bar 對 navigation bar 的 padding 由 bottom bar 自身直接計算。
//                    // 不過這樣會導致屏幕顯示内容和底部 navigation bar 相重叠，需要另設 content padding。
//                    WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
//                )
            ) {
                composable(BangAppScreen.Home.name) {
                    val viewModel: HomeViewModel = viewModel()
                    HomeScreen(
                        { showNavBar = !showNavBar },
                        viewModel = viewModel,
                        innerPadding,
                    )
                }
                composable(BangAppScreen.Gallery.name) {
                    GalleryScreen(
                        { showNavBar = it }
                    )
                }
                composable(BangAppScreen.Favorite.name) {
                    FavoriteScreen()
                }
                composable(BangAppScreen.Stickers.name) {
                    StickersScreen()
                }

                //TODO: 貼紙界面？
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BangDreamGalleryTheme {
        BangApp()
    }
}