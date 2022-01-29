package `fun`.lifeupapp.calmanager.ui.theme.m3

import `fun`.lifeupapp.calmanager.ui.theme.m3.green.GreenAppTheme
import `fun`.lifeupapp.calmanager.ui.theme.m3.pink.PinkAppTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlin.random.Random

private val isUsingGreenTheme = Random.nextBoolean()

/**
 * Randomly select an app theme with the app lifecycle.
 */
@Composable
fun CalendarManagerM3Theme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    if (isUsingGreenTheme) {
        GreenAppTheme(useDarkTheme, content)
    } else {
        PinkAppTheme(useDarkTheme, content)
    }
}