package com.anyremote.remote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anyremote.remote.ui.DashboardScreen
import com.anyremote.remote.ui.RemoteControllerScreen
import com.anyremote.remote.ui.MatchIRScreen
import com.anyremote.remote.ui.SettingsScreen
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.LocaleManager
import com.anyremote.remote.ui.theme.DeepBackground
import com.anyremote.remote.ui.theme.AnyRemoteTheme

class MainActivity : ComponentActivity() {
    private var navController: NavHostController? = null

    override fun attachBaseContext(newBase: android.content.Context) {
        LocaleManager.init(newBase)
        val lang = LocaleManager.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleManager.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.anyremote.remote.data.RemoteDatabase.init(applicationContext)
        FavoriteManager.init(applicationContext)
        
        val initDeviceId = intent?.getIntExtra("deviceId", -1) ?: -1
        val initBrandId = intent?.getIntExtra("brandId", -1) ?: -1
        
        setContent {
            val currentThemeMode = FavoriteManager.currentThemeState.value
            val darkTheme = when (currentThemeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            AnyRemoteTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val controller = rememberNavController()
                    navController = controller
                    AppNavigation(controller, initDeviceId, initBrandId)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deviceId = intent.getIntExtra("deviceId", -1)
        val brandId = intent.getIntExtra("brandId", -1)
        if (deviceId != -1 && brandId != -1) {
            navController?.navigate("remote/$deviceId/$brandId") {
                popUpTo("dashboard") { saveState = false }
                launchSingleTop = true
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    initDeviceId: Int,
    initBrandId: Int
) {
    val startDest = if (initDeviceId != -1 && initBrandId != -1) {
        "remote/$initDeviceId/$initBrandId"
    } else {
        "dashboard"
    }
    
    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("dashboard") {
            DashboardScreen(
                onDeviceSelected = { deviceId, brandId ->
                    navController.navigate("match_ir/$deviceId/$brandId")
                },
                onFavoriteSelected = { deviceId, brandId ->
                    navController.navigate("remote/$deviceId/$brandId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        composable(
            route = "match_ir/{deviceId}/{brandId}",
            arguments = listOf(
                navArgument("deviceId") { type = NavType.IntType },
                navArgument("brandId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getInt("deviceId") ?: 1
            val brandId = backStackEntry.arguments?.getInt("brandId") ?: 1
            
            MatchIRScreen(
                deviceId = deviceId,
                brandId = brandId,
                onMatchSuccess = {
                    navController.navigate("remote/$deviceId/$brandId") {
                        popUpTo("dashboard")
                    }
                },
                onSaveAndReturnToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "remote/{deviceId}/{brandId}",
            arguments = listOf(
                navArgument("deviceId") { type = NavType.IntType },
                navArgument("brandId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getInt("deviceId") ?: 1
            val brandId = backStackEntry.arguments?.getInt("brandId") ?: 1
            
            RemoteControllerScreen(
                deviceId = deviceId,
                brandId = brandId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
