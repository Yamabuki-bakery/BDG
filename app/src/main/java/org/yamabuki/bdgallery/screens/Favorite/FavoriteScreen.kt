package org.yamabuki.bdgallery.screens.Favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.components.BangAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(){
    val allScreens = BangAppScreen.values().toList()
    val currentScreen = BangAppScreen.Favorite
    Scaffold(
        topBar = {
            BangAppBar(currentScreen)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = "This is Favorite")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    FavoriteScreen()
}