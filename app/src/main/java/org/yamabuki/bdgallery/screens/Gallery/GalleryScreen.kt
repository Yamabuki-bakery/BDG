package org.yamabuki.bdgallery.screens.Gallery

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.components.BangAppBar
import org.yamabuki.bdgallery.dataType.Card

val currentScreen = BangAppScreen.Gallery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    showNavBar: (Boolean) -> Unit,
    viewModel: GalleryViewModel = viewModel(),
    fatherInnerPadding: PaddingValues,
) {
    val allScreens = BangAppScreen.values().toList()
    val systemUiController = rememberSystemUiController()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        rememberTopAppBarScrollState()
    )
    val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
        .containerColor(scrollFraction = scrollBehavior.scrollFraction).value


    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, statusBarColor.luminance() > 0.5)
        //systemUiController.setStatusBarColor(statusBarColor)
        if (scrollBehavior.state.offset == 0f){
            showNavBar(true)
        }else{
            showNavBar(false)
        }
       // Log.d("Scroll frac", scrollBehavior.state.offset.toString())
    }

    LaunchedEffect(Unit) {
       // viewModel.init()
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            GalleryAppbar(
                scrollBehavior,
                { viewModel.setLayout() }
            )
        }
    ) { innerPadding ->

        val listPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )

        when (viewModel.layout) {
            GalleryLayout.Metadata -> MetadataLazyList(
                cards = viewModel.cards,
                contentPadding = listPadding
            )

            GalleryLayout.LargeImage -> LargeImageLazyList(
                cards = viewModel.cards,
                contentPadding = listPadding
            )

            GalleryLayout.Grid -> LazyGrid(
                cards = viewModel.cards,
                contentPadding = listPadding
            )
        }
    }
}

@Composable
private fun LazyGrid(
    cards: List<Card>,
    contentPadding: PaddingValues
){
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.SpaceAround
    ){
        item { Text(text = "Grid 模式，還沒寫好捏") }
    }
}

@Composable
private fun LargeImageLazyList(
    cards: List<Card>,
    contentPadding: PaddingValues
){
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.SpaceAround
    ){
        item { Text(text = "Large Image, 還沒寫好捏") }
    }
}

@Composable
private fun MetadataLazyList(
    cards: List<Card>,
    contentPadding: PaddingValues
){
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.SpaceAround
    ){
        items(
            cards,
            key = { it.id },
            contentType = { Card::javaClass }
        ) {
            Text(
                text = "Title ${it.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(20.dp, 4.dp)
            )
        }
    }
}

@Composable
private fun GalleryAppbar(
    scrollBehavior: TopAppBarScrollBehavior,
    onLayoutChangeClicked: () -> Unit = {},
) {
    // Fuck you Google
    BangAppBar(
        currentScreen = BangAppScreen.Gallery,
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = { onLayoutChangeClicked() }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Change Layout"
                )
            }
        },
    )
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    GalleryScreen(
//        {}
//    )
}