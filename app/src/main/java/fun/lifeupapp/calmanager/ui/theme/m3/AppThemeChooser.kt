package `fun`.lifeupapp.calmanager.ui.theme.m3

import `fun`.lifeupapp.calmanager.ui.theme.m3.blue.BlueAppTheme
import `fun`.lifeupapp.calmanager.ui.theme.m3.green.GreenAppTheme
import `fun`.lifeupapp.calmanager.ui.theme.m3.pink.PinkAppTheme
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import kotlin.random.Random

private var themeRandomInt = Random.nextInt(0, 3)

fun rerollTheme() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        themeRandomInt = Random.nextInt(0, 3)
    }
}

/**
 * Randomly select an app theme with the app lifecycle.
 */
@Composable
fun CalendarManagerM3Theme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    when (themeRandomInt) {
        0 -> PinkAppTheme(useDarkTheme, content)
        1 -> GreenAppTheme(useDarkTheme, content)
        else -> BlueAppTheme(useDarkTheme, content)
    }
}

fun applyDynamicColorsIfAvailable(
    context: Context,
    useDarkTheme: Boolean,
    defaultColorScheme: ColorScheme
): ColorScheme {
    // Dynamic color is available on Android 12+
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return when {
        dynamicColor && useDarkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && !useDarkTheme -> dynamicLightColorScheme(context)
        else -> defaultColorScheme
    }
}