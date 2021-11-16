package `fun`.lifeupapp.calmanager

import `fun`.lifeupapp.calmanager.ui.page.about.About
import `fun`.lifeupapp.calmanager.ui.page.home.Home
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    @ExperimentalPermissionsApi
    @ExperimentalUnitApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            val darkMode = isSystemInDarkTheme()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    Home(navController)
                }
                composable("about") {
                    About()
                    if(darkMode){
                        window?.statusBarColor = Color.Black.toArgb()
                    }
                    else
                    {
                        window?.statusBarColor = Color.White.toArgb()
                    }
                }
            }
        }
    }


}