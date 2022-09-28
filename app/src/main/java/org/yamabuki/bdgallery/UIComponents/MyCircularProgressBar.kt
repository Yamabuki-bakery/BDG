package org.yamabuki.bdgallery.UIComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.yamabuki.bdgallery.ui.theme.myColors


@Composable
fun MyCircularProgressBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.myColors.progressBarColor,
    progress: Float? = null,
    contentAlignment: Alignment = Alignment.TopEnd
){
    val progressBarModifier = modifier
        .padding(horizontal = 10.dp, vertical = 10.dp)
        .height(15.dp)
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
    ){
        if (progress == null){
            CircularProgressIndicator(
                modifier = progressBarModifier,
                color = color,
                strokeWidth = 2.dp
            )
        }else{
            CircularProgressIndicator(
                progress = progress,
                modifier = progressBarModifier,
                color = color,
                strokeWidth = 2.dp
            )
        }
    }
}
