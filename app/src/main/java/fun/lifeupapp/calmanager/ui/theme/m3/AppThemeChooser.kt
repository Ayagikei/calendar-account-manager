package `fun`.lifeupapp.calmanager.ui.theme.m3

import `fun`.lifeupapp.calmanager.ui.theme.m3.blue.BlueAppTheme
import `fun`.lifeupapp.calmanager.ui.theme.m3.green.GreenAppTheme
import `fun`.lifeupapp.calmanager.ui.theme.m3.pink.PinkAppTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlin.random.Random

private val themeRandomInt = Random.nextInt(0, 3)

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