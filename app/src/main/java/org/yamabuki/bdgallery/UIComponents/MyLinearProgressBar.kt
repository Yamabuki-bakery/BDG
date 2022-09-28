package org.yamabuki.bdgallery.UIComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yamabuki.bdgallery.ui.theme.myColors

@Composable
fun MyLinearProgressBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.myColors.progressBarColor,
    trackColor: Color = MaterialTheme.myColors.progressBarBg,
){
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ){
        LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 0.dp),
            color = color,
            trackColor = trackColor
        )
    }

}

@Composable
fun MyLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.myColors.progressBarColor,
    trackColor: Color = MaterialTheme.myColors.progressBarBg,
){
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ){
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 0.dp),
            color = color,
            trackColor = trackColor
        )
    }
}