package org.yamabuki.bdgallery.screens.Home

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.components.BangAppBar
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.format.DateTimeFormatter


val currentScreen = BangAppScreen.Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClicked: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    fatherInnerPadding: PaddingValues,
) {
    val allScreens = BangAppScreen.values().toList()
    val systemUiController = rememberSystemUiController()
    val scrollState = rememberScrollState()

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        //decayAnimationSpec,
        rememberTopAppBarScrollState()
    )
    val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
        .containerColor(scrollFraction = scrollBehavior.scrollFraction).value


    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, statusBarColor.luminance() > 0.5)
        //systemUiController.setStatusBarColor(  statusBarColor)
    }
    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                // 底部 padding 從 activity 拿到的 bottom bar 高度
            .padding(bottom = fatherInnerPadding.calculateBottomPadding()),
        topBar = {
            HomeAppbar(
                onSettingsClicked,
                scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
            .padding(innerPadding)
            .verticalScroll(scrollState),
        ) {
            HomeCard(elevated = true) {
                Text(text = "Last Update:")
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = viewModel.lastUpdateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            }

            if (viewModel.appUpdate){
                HomeCard() {
                    Text(text = "TEST 2")
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Manga count: ${viewModel.mangaCount}")
                }
            }

            HomeCard() {
                Text(text = "TEST 3")
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "Sticker count: ${viewModel.stickerCount}")
            }

            if (!viewModel.appUpdate){
                HomeCard() {
                    Text(text = "TEST 2")
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(text = "Manga count: ${viewModel.mangaCount}")
                }
            }

            HomeCard() {
                Text(text = "TEST 2")
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = "Manga count: ${viewModel.mangaCount}")
            }

            HomeCard() {
                Text(text = "TEST 4 Card Count")
                Spacer(modifier = Modifier.padding(8.dp))
                for (i in viewModel.cardCountByStar.indices.reversed()){
                    val star = i + 1
                    Text(
                        text = "${"*".repeat(star)}${" ".repeat(5 - star)}${ viewModel.cardCountByStar[i]}",
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = viewModel.state.name)
            Spacer(modifier = Modifier.padding(8.dp))
            Row(modifier = Modifier.padding(innerPadding)) {
                Button(onClick = { viewModel.refresh() }) {
                    Text(text = "LOAD")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = { viewModel.clear() }) {
                    Text(text = "Clear")
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())  // 防止和底部導航欄重叠
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeCard (
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
){
    if (elevated){
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = colors,
            elevation = elevation,
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }else{
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = colors,
            border = BorderStroke(1.dp, Color.DarkGray),
            elevation = elevation,
        ) {
            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
            ) {
                content()
            }
        }
    }

}

@Composable
private fun HomeAppbar(
    onSettingsClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    BangAppBar(
        currentScreen = BangAppScreen.Home,
        navigationIcon = {
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            IconButton(onClick = { onSettingsClicked() }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    HomeScreen(
//        {},
//        //_,_->},
//        PaddingValues(0,0,0,0)
//    )
}