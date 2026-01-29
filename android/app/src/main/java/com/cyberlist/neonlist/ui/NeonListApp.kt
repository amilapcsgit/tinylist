package com.cyberlist.neonlist.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
fun NeonListApp(viewModel: AppViewModel) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = "home",
    modifier = Modifier
  ) {
    composable("home") {
      HomeScreen(
        viewModel = viewModel,
        onOpenList = { navController.navigate("list/$it") },
        onOpenSearch = { navController.navigate("search") },
        onOpenSettings = { navController.navigate("settings") }
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
        onBack = { navController.popBackStack() }
      )
    }
    composable("search") {
      SearchScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
        onOpenList = { navController.navigate("list/$it") }
      )
    }
    composable("settings") {
      SettingsScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
  }
}
