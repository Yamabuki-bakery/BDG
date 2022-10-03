package org.yamabuki.bdgallery.components.AppBarControl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy


class EnterAlwaysState(
    setHeight: Int,
    scrollOffset: Float = 0f
) : AppBarScrollState(setHeight) {

    override var _scrollOffset by mutableStateOf(
        value = scrollOffset,
        policy = structuralEqualityPolicy()
    )

    override val offset: Float
        get() = -scrollOffset.coerceIn(0f, setHeight.toFloat())

    override var scrollOffset: Float
        get() = _scrollOffset
        set(value) {
            val oldOffset = _scrollOffset
            _scrollOffset = value.coerceIn(0f, setHeight.toFloat())
            _consumed = oldOffset - _scrollOffset
        }

    companion object {
        val Saver = run {

            val setHeightKey = "SetHeight"
            val scrollOffsetKey = "ScrollOffset"

            mapSaver(
                save = {
                    mapOf(
                        setHeightKey to it.setHeight,
                        scrollOffsetKey to it.scrollOffset
                    )
                },
                restore = {
                    EnterAlwaysState(
                        setHeight = (it[setHeightKey] as Int),
                        scrollOffset = it[scrollOffsetKey] as Float,
                    )
                }
            )
        }
    }
}