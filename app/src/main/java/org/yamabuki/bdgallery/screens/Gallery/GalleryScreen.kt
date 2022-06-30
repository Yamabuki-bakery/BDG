package org.yamabuki.bdgallery.screens.Gallery

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

val currentScreen = BangAppScreen.Gallery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    showNavBar: (Boolean) -> Unit,
    viewModel: GalleryViewModel = viewModel()
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


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            GalleryAppbar(
                scrollBehavior,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            items(100) { count ->
                Text(
                    text = "Item ${count + 1}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(20.dp, 4.dp)
                )
            }
        }
    }
}

@Composable
private fun GalleryAppbar(
    //onSettingsClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    // Fuck you Google
    BangAppBar(
        currentScreen = BangAppScreen.Gallery,
        scrollBehavior = scrollBehavior,
    )
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GalleryScreen(
        {}
    )
}