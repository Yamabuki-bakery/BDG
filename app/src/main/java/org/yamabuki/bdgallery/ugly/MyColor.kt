package org.yamabuki.bdgallery.ugly

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

data class MyColorScheme (
    val materialColorScheme: ColorScheme,
    // my custom colors comes here
    val powerfulCardBg: Color,
    val coolCardBg: Color,
    val happyCardBg: Color,
    val pureCardBg: Color,
    ) {
    val primary: Color get() = materialColorScheme.primary
    val onPrimary: Color get() = materialColorScheme.onPrimary
    val primaryContainer: Color get() = materialColorScheme.primaryContainer
    val onPrimaryContainer: Color get() = materialColorScheme.onPrimaryContainer
    val inversePrimary: Color get() = materialColorScheme.inversePrimary
    val secondary: Color get() = materialColorScheme.secondary
    val onSecondary: Color get() = materialColorScheme.onSecondary
    val secondaryContainer: Color get() = materialColorScheme.secondaryContainer
    val onSecondaryContainer: Color get() = materialColorScheme.onSecondaryContainer
    val tertiary: Color get() = materialColorScheme.tertiary
    val onTertiary: Color get() = materialColorScheme.onTertiary
    val tertiaryContainer: Color get() = materialColorScheme.tertiaryContainer
    val onTertiaryContainer: Color get() = materialColorScheme.onTertiaryContainer
    val background: Color get() = materialColorScheme.background
    val onBackground: Color get() = materialColorScheme.onBackground
    val surface: Color get() = materialColorScheme.surface
    val onSurface: Color get() = materialColorScheme.onSurface
    val surfaceVariant: Color get() = materialColorScheme.surfaceVariant
    val onSurfaceVariant: Color get() = materialColorScheme.onSurfaceVariant
    val surfaceTint: Color get() = primary
    val inverseSurface: Color get() = materialColorScheme.inverseSurface
    val inverseOnSurface: Color get() = materialColorScheme.inverseOnSurface
    val error: Color get() = materialColorScheme.error
    val onError: Color get() = materialColorScheme.onError
    val errorContainer: Color get() = materialColorScheme.errorContainer
    val onErrorContainer: Color get() = materialColorScheme.onErrorContainer
    val outline: Color get() = materialColorScheme.outline
}