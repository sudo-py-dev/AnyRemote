package com.anyremote.remote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.anyremote.remote.data.RemoteDatabase
import com.anyremote.remote.service.FavoriteManager
import com.anyremote.remote.service.LocaleManager
import com.anyremote.remote.service.ShortcutHelper
import com.anyremote.remote.ui.screens.DashboardScreen
import com.anyremote.remote.ui.screens.MatchIRScreen
import com.anyremote.remote.ui.screens.RemoteControllerScreen
import com.anyremote.remote.ui.screens.SettingsScreen
import com.anyremote.remote.ui.theme.AnyRemoteTheme
import com.anyremote.remote.update.UpdatePrompt
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute

@Serializable data object SettingsRoute

@Serializable data class MatchIrRoute(val deviceId: Int, val brandId: Int)

@Serializable data class RemoteRoute(val deviceId: Int, val brandId: Int)

class MainActivity : ComponentActivity() {
    private var navController: NavHostController? = null

    override fun attachBaseContext(newBase: Context) {
        LocaleManager.init(newBase)
        super.attachBaseContext(
            LocaleManager.applyLocale(newBase, LocaleManager.getSavedLanguage(newBase))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        RemoteDatabase.init(applicationContext)
        FavoriteManager.init(applicationContext)

        // Only honor a shortcut launch on a fresh start, not after rotation or process restore.
        val startRemote = if (savedInstanceState == null) intent.remoteRoute(this) else null

        setContent {
            val darkTheme =
                when (FavoriteManager.currentThemeState.value) {
                    "light" -> false
                    "dark" -> true
                    else -> isSystemInDarkTheme()
                }

            // System bar icons follow the in-app theme, not just the system setting.
            DisposableEffect(darkTheme) {
                val style =
                    if (darkTheme) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    else
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        )
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            AnyRemoteTheme(
                darkTheme = darkTheme,
                dynamicColor = FavoriteManager.currentDynamicColorState.value,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val controller = rememberNavController()
                    navController = controller
                    AppNavigation(controller, startRemote)
                    UpdatePrompt()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action != Intent.ACTION_VIEW) return
        val route = intent.remoteRoute(this)
        if (route != null) {
            navController?.openRemote(route)
        } else {
            navController?.popBackStack(DashboardRoute, inclusive = false)
        }
    }
}

/** Launcher shortcuts carry deviceId/brandId extras pointing at a saved remote. */
private fun Intent?.remoteRoute(context: Context): RemoteRoute? {
    if (this?.action != Intent.ACTION_VIEW) return null
    val deviceId = getIntExtra("deviceId", -1)
    val brandId = getIntExtra("brandId", -1)
    val device = RemoteDatabase.deviceTypes.firstOrNull { it.id == deviceId } ?: return null
    if (device.brands.none { it.id == brandId }) return null
    if (!FavoriteManager.isFavorite(context, deviceId, brandId)) {
        ShortcutHelper.syncSavedShortcut(context, deviceId, brandId)
        return null
    }
    return RemoteRoute(deviceId, brandId)
}

private fun NavHostController.openRemote(route: RemoteRoute) =
    navigate(route) {
        popUpTo(DashboardRoute)
        launchSingleTop = true
    }

@Composable
fun AppNavigation(
    navController: NavHostController,
    startRemote: RemoteRoute?,
) {
    var startHandled by rememberSaveable { mutableStateOf(false) }

    val forward = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        enterTransition = {
            fadeIn(tween(220)) +
                slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { forward * it / 8 }
        },
        exitTransition = {
            fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { -forward * it / 16 }
        },
        popEnterTransition = {
            fadeIn(tween(220)) +
                slideInHorizontally(tween(250, easing = FastOutSlowInEasing)) { -forward * it / 16 }
        },
        popExitTransition = {
            fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { forward * it / 8 }
        },
    ) {
        composable<DashboardRoute> {
            LaunchedEffect(Unit) {
                if (!startHandled && startRemote != null) {
                    startHandled = true
                    navController.openRemote(startRemote)
                }
            }

            DashboardScreen(
                onDeviceSelected = { deviceId, brandId ->
                    navController.navigate(MatchIrRoute(deviceId, brandId))
                },
                onFavoriteSelected = { deviceId, brandId ->
                    navController.navigate(RemoteRoute(deviceId, brandId))
                },
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
        }
        composable<MatchIrRoute> { entry ->
            val route = entry.toRoute<MatchIrRoute>()
            MatchIRScreen(
                deviceId = route.deviceId,
                brandId = route.brandId,
                onMatchSuccess = {
                    navController.openRemote(RemoteRoute(route.deviceId, route.brandId))
                },
                onSaveAndReturnToDashboard = {
                    navController.popBackStack(DashboardRoute, inclusive = false)
                },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<RemoteRoute> { entry ->
            val route = entry.toRoute<RemoteRoute>()
            RemoteControllerScreen(
                deviceId = route.deviceId,
                brandId = route.brandId,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<SettingsRoute> { SettingsScreen(onBackClick = { navController.popBackStack() }) }
    }
}
