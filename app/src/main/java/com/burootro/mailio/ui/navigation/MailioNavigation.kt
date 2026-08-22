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
import com.burootro.mailio.ui.screens.appeals.MyAppealsScreen
import com.burootro.mailio.ui.screens.home.HomeScreen
import com.burootro.mailio.ui.screens.inbox.InboxScreen
import com.burootro.mailio.ui.screens.message.MessageScreen
import com.burootro.mailio.ui.screens.settings.SettingsScreen
import com.burootro.mailio.ui.screens.signin.SignInScreen
import com.burootro.mailio.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val SIGN_IN = "signin"
    const val HOME = "home"
    const val INBOX = "inbox"
    const val MESSAGE = "message"
    const val SETTINGS = "settings"
    const val APPEALS = "appeals"
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
                onNavigateToSignIn = {
                    navController.navigate(Routes.SIGN_IN) {
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
            route = Routes.SIGN_IN,
            enterTransition = { fadeIn(tween(600)) },
            exitTransition = { fadeOut(tween(400)) }
        ) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            enterTransition = { fadeIn(tween(500)) },
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
            arguments = listOf(navArgument("addressId") { type = NavType.StringType }),
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
            arguments = listOf(navArgument("messageId") { type = NavType.StringType }),
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
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAppealsClick = {
                    navController.navigate(Routes.APPEALS)
                }
            )
        }

        composable(
            route = Routes.APPEALS,
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
            MyAppealsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
