package org.yamabuki.bdgallery.screens.Home

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.components.BangAppBar
import androidx.lifecycle.viewmodel.compose.viewModel


val currentScreen = BangAppScreen.Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClicked: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val allScreens = BangAppScreen.values().toList()
    val systemUiController = rememberSystemUiController()

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
        viewModel.countAllCards()
        viewModel.countAllManga()
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeAppbar(
                onSettingsClicked,
                scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = "Card count: ${viewModel.cardCount}")
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = "Manga count: ${viewModel.mangaCount}")
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
    HomeScreen(
        {},
        //_,_->}
    )
}