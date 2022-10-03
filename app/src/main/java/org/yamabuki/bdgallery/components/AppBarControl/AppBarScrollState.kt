package org.yamabuki.bdgallery.components.AppBarControl

import androidx.compose.ui.unit.Dp

abstract class AppBarScrollState(val setHeight: Int) : AppBarState {

    protected var _consumed: Float = 0f

    protected abstract var _scrollOffset: Float

    final override val height: Float
        get() = setHeight.toFloat()//(setHeight - scrollOffset)//.coerceIn(minHeight.toFloat(), maxHeight.toFloat())

    final override val consumed: Float
        get() = _consumed

    final override var scrollTopLimitReached: Boolean = true
}