package org.yamabuki.bdgallery.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.shapes
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import org.yamabuki.bdgallery.ugly.MyColorScheme

private val DarkColorScheme2 = MyColorScheme(
    darkColors(
        primary = Purple200,
        primaryVariant = Purple700,
        secondary = Teal200
        ),
    powerfulCardBg = Color(0xFFB71C1C),
    coolCardBg = Color(0xFF1854B3),
    happyCardBg = Color(0xFFBE5300),
    pureCardBg = Color(0xFF449126),
    progressBarColor = Shironeri,
    progressBarBg = TransWhite,
)


private val LightColorScheme2 = MyColorScheme(
    lightColors(
        primary = Purple500,
        primaryVariant = Purple700,
        secondary = Teal200
    ),
    powerfulCardBg = Color(0xFFFF76A6),
    coolCardBg = Color(0xFF7DA2FF),
    happyCardBg = Color(0xFFFDAB85),
    pureCardBg = Color(0xFFC1F8C3),
    progressBarColor = Sumi,
    progressBarBg = TransBlack,
)


private val LocalColors = staticCompositionLocalOf { LightColorScheme2 }


@Composable
fun BangDreamGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    //dynamicColor: Boolean = true,
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

//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            //(view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
//            (view.context as Activity).window.statusBarColor = Transparent.toArgb()
//            (view.context as Activity).window.navigationBarColor = Transparent.toArgb()
//            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
//        }
//    }

    CompositionLocalProvider(LocalColors provides colorScheme) {
        MaterialTheme(
            colors = colorScheme.materialColorScheme,
            typography = Typography,
            shapes = shapes,
            content = content,
        )
    }

}
val androidx.compose.material.MaterialTheme.myColors: MyColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current