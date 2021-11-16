package `fun`.lifeupapp.calmanager.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = MyDarkPinkPrimaryTextColor,
    primaryVariant = MYPinkPrimaryTextColor,
    secondary = MyDarkPinkPrimaryTextColor,
    secondaryVariant = white2,
    background = MyDarkPinkBackground,
    onBackground = black2,
    surface = black2
)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    primary = MYPinkPrimaryTextColor,
    primaryVariant = MyDarkPinkPrimaryTextColor,
    secondary = MYPinkPrimaryTextColor,
    background = MYPinkBackground,
    onBackground = black2,
    surface = white2

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun CalendarManagerTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}