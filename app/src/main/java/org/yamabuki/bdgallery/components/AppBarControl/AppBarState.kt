package org.yamabuki.bdgallery.components.AppBarControl

import androidx.compose.runtime.Stable

@Stable
interface AppBarState {
    val offset: Float
    val height: Float
 //   val progress: Float
    val consumed: Float
    var scrollTopLimitReached: Boolean
    var scrollOffset: Float
}