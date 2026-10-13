package com.burootro.mailio.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.burootro.mailio.DeepLink
import com.burootro.mailio.ui.screens.appeals.MyAppealsScreen
import com.burootro.mailio.ui.screens.contact.ContactScreen
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
    const val CONTACT = "contact"

    fun inbox(addressId: String) = "$INBOX/$addressId"
    fun message(messageId: String) = "$MESSAGE/$messageId"
}

private const val DURATION = 300

// The app is right-to-left: going "forward" slides in from the left edge.
private val forwardEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { -it / 5 } + fadeIn(tween(DURATION))
}
private val forwardExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { it / 10 } + fadeOut(tween(DURATION / 2))
}
private val backEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { it / 10 } + fadeIn(tween(DURATION))
}
private val backExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { -it / 5 } + fadeOut(tween(DURATION / 2))
}

@Composable
fun MailioNavigation(
    deepLink: DeepLink?,
    onDeepLinkHandled: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    // Open the message from a notification once the user is inside the app.
    LaunchedEffect(deepLink, route) {
        val link = deepLink ?: return@LaunchedEffect
        if (route == null || route == Routes.SPLASH || route == Routes.SIGN_IN) return@LaunchedEffect
        link.addressId?.let { navController.navigate(Routes.inbox(it)) { launchSingleTop = true } }
        link.messageId?.let { navController.navigate(Routes.message(it)) }
        onDeepLinkHandled()
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = forwardEnter,
        exitTransition = forwardExit,
        popEnterTransition = backEnter,
        popExitTransition = backExit
    ) {
        composable(
            route = Routes.SPLASH,
            exitTransition = { fadeOut(tween(220)) }
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
            enterTransition = { fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.97f) },
            exitTransition = { fadeOut(tween(220)) }
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
            enterTransition = { fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.97f) }
        ) {
            HomeScreen(
                onAddressClick = { address -> navController.navigate(Routes.inbox(address.id)) },
                onAddressCreated = { addressId -> navController.navigate(Routes.inbox(addressId)) },
                onMessageClick = { message -> navController.navigate(Routes.message(message.id)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = "${Routes.INBOX}/{addressId}",
            arguments = listOf(navArgument("addressId") { type = NavType.StringType })
        ) {
            InboxScreen(
                onBack = { navController.popBackStack() },
                onMessageClick = { message -> navController.navigate(Routes.message(message.id)) }
            )
        }

        composable(
            route = "${Routes.MESSAGE}/{messageId}",
            arguments = listOf(navArgument("messageId") { type = NavType.StringType })
        ) {
            MessageScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAppealsClick = { navController.navigate(Routes.APPEALS) },
                onContactClick = { navController.navigate(Routes.CONTACT) }
            )
        }

        composable(Routes.APPEALS) {
            MyAppealsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.CONTACT) {
            ContactScreen(onBack = { navController.popBackStack() })
        }
    }
}
