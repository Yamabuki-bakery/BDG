package org.yamabuki.bdgallery.screens.Stickers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.yamabuki.bdgallery.BangAppScreen
import org.yamabuki.bdgallery.components.BangAppBar


@Composable
fun StickersScreen(){
    val allScreens = BangAppScreen.values().toList()
    val currentScreen = BangAppScreen.Stickers
    Scaffold(
        topBar = {
            BangAppBar(currentScreen, modifier = Modifier)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = "This is Stickers")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    StickersScreen()
}