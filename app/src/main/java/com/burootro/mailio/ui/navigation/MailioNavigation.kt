package com.burootro.mailio.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.burootro.mailio.ui.screens.home.HomeScreen
import com.burootro.mailio.ui.screens.inbox.InboxScreen
import com.burootro.mailio.ui.screens.message.MessageScreen
import com.burootro.mailio.ui.screens.onboarding.OnboardingScreen
import com.burootro.mailio.ui.screens.restore.RestoreScreen
import com.burootro.mailio.ui.screens.settings.SettingsScreen
import com.burootro.mailio.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val RESTORE = "restore"
    const val HOME = "home"
    const val INBOX = "inbox"
    const val MESSAGE = "message"
    const val SETTINGS = "settings"
}

@Composable
fun MailioNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        composable(
            route = Routes.SPLASH,
            exitTransition = { fadeOut(tween(600)) }
        ) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.ONBOARDING,
            enterTransition = { fadeIn(tween(600)) },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(450)
                ) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(400)
                ) + fadeIn(tween(400))
            }
        ) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onRestoreClick = {
                    navController.navigate(Routes.RESTORE)
                }
            )
        }

        composable(
            route = Routes.RESTORE,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeOut(tween(300))
            }
        ) {
            RestoreScreen(
                onBack = { navController.popBackStack() },
                onRestored = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = tween(450)
                ) + fadeIn(tween(450))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(400)
                ) + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(400)
                ) + fadeIn(tween(400))
            }
        ) {
            HomeScreen(
                onAddressClick = { address ->
                    navController.navigate("${Routes.INBOX}/${address.id}")
                },
                onAddressCreated = { addressId ->
                    navController.navigate("${Routes.INBOX}/$addressId")
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = "${Routes.INBOX}/{addressId}",
            arguments = listOf(
                navArgument("addressId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(400)
                ) + fadeOut(tween(250))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(400)
                ) + fadeIn(tween(400))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeOut(tween(300))
            }
        ) {
            InboxScreen(
                onBack = { navController.popBackStack() },
                onMessageClick = { message ->
                    navController.navigate("${Routes.MESSAGE}/${message.id}")
                }
            )
        }

        composable(
            route = "${Routes.MESSAGE}/{messageId}",
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeOut(tween(300))
            }
        ) {
            MessageScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SETTINGS,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400)
                ) + fadeOut(tween(300))
            }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
