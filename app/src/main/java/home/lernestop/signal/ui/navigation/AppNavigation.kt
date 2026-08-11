package home.lernestop.signal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import home.lernestop.signal.ui.screen.home.HomeScreen
import home.lernestop.signal.ui.screen.home.HomeViewModel
import home.lernestop.signal.ui.screen.signal.SignalScreen
import home.lernestop.signal.ui.screen.signal.SignalViewModel

@Composable
fun AppNavigation(externalIntentViewModel: ExternalIntentViewModel = viewModel()) {
    val navController = rememberNavController()

    val externalLink by externalIntentViewModel.externalLink.collectAsStateWithLifecycle()

    LaunchedEffect(externalLink) {
        externalLink?.let { link ->
            navController.navigate(StartRoute(link)) {
                popUpTo(0) { inclusive = true }
            }
            externalIntentViewModel.consumeLink()
        }
    }

    NavHost(
        navController = navController,
        startDestination = StartRoute(null),
    ) {
        composable<StartRoute> {
            val vm: HomeViewModel = hiltViewModel()

            HomeScreen(vm) { idItem ->
                navController.navigate(SignalRoute(idItem))
            }
        }

        composable<SignalRoute> {
            val vm: SignalViewModel = hiltViewModel()

            SignalScreen(vm) {
                navController.popBackStack()
            }
        }
    }
}