package com.dmesh.prototype.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dmesh.prototype.MeshController
import com.dmesh.prototype.ui.MeshViewModel
import com.dmesh.prototype.ui.screens.HomeScreen
import com.dmesh.prototype.ui.screens.LogsScreen
import com.dmesh.prototype.ui.screens.MapScreen
import com.dmesh.prototype.ui.screens.MeshScreen
import com.dmesh.prototype.ui.screens.MessagesScreen
import com.dmesh.prototype.ui.screens.NodesScreen
import com.dmesh.prototype.ui.screens.SettingsScreen
import com.dmesh.prototype.ui.screens.SimulationScreen

sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "Home")
    data object Messages : Screen("messages", "Messages")
    data object Mesh : Screen("mesh", "Mesh")
    data object Map : Screen("map", "Map")
    data object Nodes : Screen("nodes", "Nodes")
    data object Logs : Screen("logs", "Logs")
    data object Simulation : Screen("simulation", "Simulation")
    data object Settings : Screen("settings", "Settings")
}

@Composable
fun DMeshNavHost(meshController: MeshController) {
    val navController = rememberNavController()
    val vm: MeshViewModel = viewModel(factory = MeshViewModelFactory(meshController))
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomItems = listOf(
        Screen.Home, Screen.Messages, Screen.Mesh, Screen.Map,
        Screen.Nodes, Screen.Logs, Screen.Simulation, Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                when (screen) {
                                    Screen.Home -> Icons.Default.Home
                                    Screen.Messages -> Icons.Default.Message
                                    Screen.Mesh -> Icons.Default.Share
                                    Screen.Map -> Icons.Default.Map
                                    Screen.Nodes -> Icons.Default.List
                                    Screen.Logs -> Icons.Default.List
                                    Screen.Simulation -> Icons.Default.Warning
                                    Screen.Settings -> Icons.Default.Settings
                                },
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) { HomeScreen(vm) }
            composable(Screen.Messages.route) { MessagesScreen(vm) }
            composable(Screen.Mesh.route) { MeshScreen(vm) }
            composable(Screen.Map.route) { MapScreen(vm) }
            composable(Screen.Nodes.route) { NodesScreen(vm) }
            composable(Screen.Logs.route) { LogsScreen(vm) }
            composable(Screen.Simulation.route) { SimulationScreen(vm) }
            composable(Screen.Settings.route) { SettingsScreen(vm) }
        }
    }
}
