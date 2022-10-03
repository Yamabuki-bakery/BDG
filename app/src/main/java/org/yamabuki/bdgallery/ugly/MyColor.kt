package org.yamabuki.bdgallery.ugly

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

data class MyColorScheme (
    val materialColorScheme: Colors,
    // my custom colors comes here
    val powerfulCardBg: Color,
    val coolCardBg: Color,
    val happyCardBg: Color,
    val pureCardBg: Color,
    val progressBarColor: Color,
    val progressBarBg: Color,
    ) {
    val primary: Color get() =  materialColorScheme.primary
    val primaryVariant: Color get() =  materialColorScheme.primaryVariant
    val secondary: Color get() =  materialColorScheme.secondary
    val secondaryVariant: Color get() =  materialColorScheme.secondaryVariant
    val background: Color get() =  materialColorScheme.background
    val surface: Color get() =  materialColorScheme.surface
    val error: Color get() =  materialColorScheme.error
    val onPrimary: Color get() =  materialColorScheme.onPrimary
    val onSecondary: Color get() =  materialColorScheme.onSecondary
    val onBackground: Color get() =  materialColorScheme.onBackground
    val onSurface: Color get() =  materialColorScheme.onSurface
    val onError: Color get() =  materialColorScheme.onError
}