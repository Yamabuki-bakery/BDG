package org.yamabuki.bdgallery.screens.Gallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.components.BangAppBar
import org.yamabuki.bdgallery.dataType.Card
import org.yamabuki.bdgallery.dataType.CardAttr
import org.yamabuki.bdgallery.ui.theme.myColors

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
    //val scrollListState = rememberLazyListState()

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
                contentPadding = listPadding,
                gridState = viewModel.lazyGridState,
 //               updateScrollPos = { a, b -> viewModel.updateScrollPos(a, b, true) }
            )

            GalleryLayout.LargeImage -> LargeImageLazyList(
                cards = viewModel.cards,
                contentPadding = listPadding,
                getStateObj = { viewModel.getLargeCardStateObj(it) },
                onCardClick = {},
                gridState = viewModel.lazyGridState,
  //              updateScrollPos = { a, b -> viewModel.updateScrollPos(a, b, true) }
            )

            GalleryLayout.Grid -> LazyGrid(
                cards = viewModel.cards,
                contentPadding = listPadding,
                gridState = viewModel.lazyGridState,
 //               updateScrollPos = { a, b -> viewModel.updateScrollPos(a, b, false) },
                onCardClick = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LazyGrid(
    cards: List<Card>,
    contentPadding: PaddingValues,
    onCardClick: () -> Unit,
  //  updateScrollPos: (Int, Int) -> Unit,
    gridState: LazyGridState,
    ){
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        contentPadding = contentPadding,
        state = gridState,
    ){
        items(
            cards,
            key = { it.id },
            contentType = { Card::javaClass }
        ){
            // todo val stateObj = getStateObj(it)
            Card(
                onClick = { onCardClick() },
                modifier = Modifier
                    .aspectRatio(1.0F)
                    .padding(4.dp),
            ) {
                Surface(
                    color = getAttrColor(card = it) ,
                    modifier = Modifier.fillMaxSize()
                    // todo: change colors based on card attribute
                ) {

                }
            }
        }
    }
//    LaunchedEffect(gridState) {
//        Log.d("[GGRID]", "LaunchedEffect")
//        snapshotFlow { gridState.firstVisibleItemIndex }
//            .distinctUntilChanged()
//            .collect {
//                updateScrollPos(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset)
//            }
//    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LargeImageLazyList(
    cards: List<Card>,
    contentPadding: PaddingValues,
    getStateObj: (Card) -> LargeImgUIState,
    onCardClick: () -> Unit,
    gridState: LazyGridState,
 //   updateScrollPos: (Int, Int) -> Unit,

){

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = contentPadding,
        state = gridState,
    ){
        items(
            cards,
            key = { it.id },
            contentType = { Card::javaClass }
        ){
            val stateObj = getStateObj(it)
            Card(
                onClick = { onCardClick() },
                modifier = Modifier
                    .aspectRatio((4.0 / 3.0).toFloat())
                    .padding(8.dp, 4.dp),
            ) {
                Surface(
                    color = getAttrColor(card = it) ,
                    modifier = Modifier.fillMaxSize()
                ) {

                }
            }
        }
    }
//    LaunchedEffect(listState) {
//        Log.d("[LargeImg]", "LaunchedEffect")
//        snapshotFlow { listState.firstVisibleItemIndex }
//            .distinctUntilChanged()
//            .collect {
//                updateScrollPos(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
//            }
//    }
}

@Composable
private fun MetadataLazyList(
    cards: List<Card>,
    contentPadding: PaddingValues,
    gridState: LazyGridState,
   // updateScrollPos: (Int, Int) -> Unit,
){
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = contentPadding,
        state = gridState,
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
//    LaunchedEffect(listState) {
//        Log.d("[MetadataLazyList]", "LaunchedEffect")
//        snapshotFlow { listState.firstVisibleItemIndex }
//            .distinctUntilChanged()
//            .collect {
//                updateScrollPos(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
//            }
//    }
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

@Composable
fun getAttrColor(card: Card): Color {
    return when (card.attribute) {
        CardAttr.POWERFUL -> MaterialTheme.myColors.powerfulCardBg
        CardAttr.COOL -> MaterialTheme.myColors.coolCardBg
        CardAttr.HAPPY -> MaterialTheme.myColors.happyCardBg
        CardAttr.PURE -> MaterialTheme.myColors.pureCardBg
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    GalleryScreen(
//        {}
//    )
}