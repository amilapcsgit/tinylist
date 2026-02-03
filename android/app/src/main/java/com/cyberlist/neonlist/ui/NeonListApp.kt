package com.cyberlist.neonlist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cyberlist.neonlist.AppViewModel
import com.cyberlist.neonlist.ui.screens.HomeScreen
import com.cyberlist.neonlist.ui.screens.ListDetailScreen
import com.cyberlist.neonlist.ui.screens.SearchScreen
import com.cyberlist.neonlist.ui.screens.SettingsScreen

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun NeonListApp(viewModel: AppViewModel) {
  val navController = rememberNavController()
  val language by viewModel.currentLanguage.collectAsState()

  val strings = when (language) {
    "it" -> ItStrings
    "si" -> SiStrings
    else -> EnStrings
  }

  CompositionLocalProvider(LocalStrings provides strings) {
    SharedTransitionLayout(
    modifier = Modifier
      .fillMaxSize()
      .systemBarsPadding()
      .clipToBounds()
  ) {
    val sharedScope = this
    NavHost(
      navController = navController,
      startDestination = "home",
      modifier = Modifier
    ) {
      composable("home") { _ ->
        HomeScreen(
          viewModel = viewModel,
          onOpenList = { navController.navigate("list/$it") },
          onOpenSearch = { navController.navigate("search") },
          onOpenSettings = { navController.navigate("settings") },
          sharedTransitionScope = sharedScope,
          animatedVisibilityScope = this
        )
      }
      composable(
        route = "list/{listId}",
        arguments = listOf(navArgument("listId") { type = NavType.StringType })
      ) { backStack ->
        val listId = backStack.arguments?.getString("listId") ?: return@composable
        ListDetailScreen(
          viewModel = viewModel,
          listId = listId,
          onBack = { navController.popBackStack() },
          sharedTransitionScope = sharedScope,
          animatedVisibilityScope = this
        )
      }
      composable("search") { backStack ->
        SearchScreen(
          viewModel = viewModel,
          onBack = { navController.popBackStack() },
          onOpenList = { navController.navigate("list/$it") }
        )
      }
      composable("settings") { backStack ->
        SettingsScreen(
          viewModel = viewModel,
          onBack = { navController.popBackStack() }
        )
      }
    }
  }
}
