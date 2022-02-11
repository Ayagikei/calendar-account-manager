package `fun`.lifeupapp.calmanager

import `fun`.lifeupapp.calmanager.ui.RouteDef
import `fun`.lifeupapp.calmanager.ui.page.about.About
import `fun`.lifeupapp.calmanager.ui.page.home.Home
import `fun`.lifeupapp.calmanager.ui.theme.m3.rerollTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Entry point of the application.
 *
 * MIT License
 * Copyright (c) 2022 AyagiKei
 */
class MainActivity : ComponentActivity() {

    @ExperimentalPermissionsApi
    @ExperimentalUnitApi
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        rerollTheme()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = RouteDef.HOME.path) {
                composable(RouteDef.HOME.path) {
                    Home(navController)
                }
                composable(RouteDef.ABOUT.path) {
                    About()
                }
            }
        }
    }

}