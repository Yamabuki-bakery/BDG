package org.yamabuki.bdgallery.screens.Gallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.UIComponents.MyCircularProgressBar
import org.yamabuki.bdgallery.components.AppBarControl.AppBarState
import org.yamabuki.bdgallery.components.AppBarControl.EnterAlwaysState
import org.yamabuki.bdgallery.components.BangAppBar
import org.yamabuki.bdgallery.dataType.Card
import org.yamabuki.bdgallery.dataType.CardAttr
import org.yamabuki.bdgallery.ui.theme.myColors

val currentScreen = BangAppScreen.Gallery


@Composable
private fun rememberToolbarState(setHeight: Dp): AppBarState {
    val height: Int = with(LocalDensity.current){
        (setHeight + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).roundToPx()
    }
    return rememberSaveable(saver = EnterAlwaysState.Saver) {

        EnterAlwaysState(height)
    }
}


@Composable
fun GalleryScreen(
    showNavBar: (Boolean) -> Unit,
    viewModel: GalleryViewModel = viewModel(),
    fatherInnerPadding: PaddingValues,
) {
    val allScreens = BangAppScreen.values().toList()
    val systemUiController = rememberSystemUiController()
    //val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
    //    rememberTopAppBarScrollState()
    //)
    //val statusBarColor = TopAppBarDefaults.centerAlignedTopAppBarColors()
    //    .containerColor(scrollFraction = scrollBehavior.scrollFraction).value


    SideEffect {
    //    systemUiController.setSystemBarsColor(Color.Transparent, statusBarColor.luminance() > 0.5)
    //    //systemUiController.setStatusBarColor(statusBarColor)
    //    if (scrollBehavior.state.offset == 0f){
    //        showNavBar(true)
    //    }else{
    //        showNavBar(false)
    //    }
       // Log.d("Scroll frac", scrollBehavior.state.offset.toString())
    }

    LaunchedEffect(Unit) {
       // viewModel.init()
    }
    //val scrollListState = rememberLazyListState()

    val toolbarState = rememberToolbarState(64.dp)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                toolbarState.scrollTopLimitReached =
                    viewModel.lazyGridState.firstVisibleItemIndex == 0 &&
                            viewModel.lazyGridState.firstVisibleItemScrollOffset == 0

                toolbarState.scrollOffset = toolbarState.scrollOffset - available.y
                return Offset(0f, toolbarState.consumed)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            GalleryAppbar(
                //scrollBehavior,
                modifier = Modifier
                    .height(
                        with(LocalDensity.current) { toolbarState.height.toDp() }
                    )
                    .graphicsLayer { translationY = toolbarState.offset },
                onLayoutChangeClicked = { viewModel.setLayout() },
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
                modifier = Modifier.graphicsLayer { translationY = toolbarState.offset }, //+ toolbarState.height },
 //               updateScrollPos = { a, b -> viewModel.updateScrollPos(a, b, true) }
            )

            GalleryLayout.LargeImage -> LargeImageLazyList(
                cards = viewModel.cards,
                contentPadding = listPadding,
                getStateObj = { viewModel.getLargeCardStateObj(it) },
                onCardClick = {},
                gridState = viewModel.lazyGridState,
                modifier = Modifier.graphicsLayer { translationY =  toolbarState.offset },

                //              updateScrollPos = { a, b -> viewModel.updateScrollPos(a, b, true) }
            )

            GalleryLayout.Grid -> LazyGrid(
                cards = viewModel.cards,
                contentPadding = listPadding,
                gridState = viewModel.lazyGridState,
 //               updateScrollPos = { a, b -> viewModel.updateScrollPos(a, b, false) },
                onCardClick = {},
                modifier = Modifier.graphicsLayer { translationY = toolbarState.offset },

                )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LazyGrid(
    cards: List<Card>,
    contentPadding: PaddingValues,
    onCardClick: () -> Unit,
  //  updateScrollPos: (Int, Int) -> Unit,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    ){
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 96.dp),
        contentPadding = contentPadding,
        state = gridState,
        modifier = modifier,
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
                    .padding(2.dp),
            ) {
                Surface(
                    color = getAttrColor(card = it) ,
                    modifier = Modifier.fillMaxSize()
                ) {

                }
            }
        }
    }

}



@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LargeImageLazyList(
    cards: List<Card>,
    contentPadding: PaddingValues,
    getStateObj: (Card) -> LargeImgUIState,
    onCardClick: () -> Unit,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,

    //   updateScrollPos: (Int, Int) -> Unit,

){

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = contentPadding,
        state = gridState,
        modifier = modifier,

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
                    when (stateObj.progress) {
                        -1 -> MyCircularProgressBar()
//                        100 -> GlideImage(
//                            imageModel = stateObj.getFile(),
//                            imageOptions = ImageOptions(
//                                contentScale = ContentScale.Fit,
//                                contentDescription = it.title,
//
//                                ),
//                            requestOptions = {
//                                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
//                            },
//                        )
                        else -> MyCircularProgressBar(progress = stateObj.progress/100F)
                    }
                }
            }
        }
    }

}

@Composable
private fun MetadataLazyList(
    cards: List<Card>,
    contentPadding: PaddingValues,
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    // updateScrollPos: (Int, Int) -> Unit,
){
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = contentPadding,
        state = gridState,
        modifier = modifier,

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
    //scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    onLayoutChangeClicked: () -> Unit = {},
) {
    // Fuck you Google
    BangAppBar(
        currentScreen = BangAppScreen.Gallery,
        //scrollBehavior = scrollBehavior,
        modifier = modifier,
        actions = {
            IconButton(onClick = { onLayoutChangeClicked() }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Change Layout"
                )
            }
        },
        height = height
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