package org.yamabuki.bdgallery.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import org.yamabuki.bdgallery.ugly.MyColorScheme

private val DarkColorScheme2 = MyColorScheme(
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        ),
    powerfulCardBg = Color(0xFFB71C1C),
    coolCardBg = Color(0xFF1854B3),
    happyCardBg = Color(0xFFBE5300),
    pureCardBg = Color(0xFF449126),
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme2 = MyColorScheme(
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40
    ),
    powerfulCardBg = Color(0xFFFF76A6),
    coolCardBg = Color(0xFF7DA2FF),
    happyCardBg = Color(0xFFFDAB85),
    pureCardBg = Color(0xFFC1F8C3),
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val LocalColors = staticCompositionLocalOf { LightColorScheme2 }


@Composable
fun BangDreamGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        //dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //    val context = LocalContext.current
        //    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        ///}
        darkTheme -> DarkColorScheme2
        else -> LightColorScheme2
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            //(view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            (view.context as Activity).window.statusBarColor = Transparent.toArgb()
            (view.context as Activity).window.navigationBarColor = Transparent.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }

    CompositionLocalProvider(LocalColors provides colorScheme) {
        MaterialTheme(
            colorScheme = colorScheme.materialColorScheme,
            typography = Typography,
            content = content
        )
    }

}
val androidx.compose.material3.MaterialTheme.myColors: MyColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current