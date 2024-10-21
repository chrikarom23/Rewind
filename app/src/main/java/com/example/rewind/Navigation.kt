package com.example.rewind

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rewind.camera.CameraScreen
import com.example.rewind.entry.DayEntry
import com.example.rewind.rewind.RewindScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.RewindScreen.route){
        composable(route = Screen.RewindScreen.route) {
            RewindScreen (navController = navController)
        }

        composable(route = Screen.EntryScreen.route) {
            DayEntry (navController = navController)
        }

        composable(route = Screen.CameraScreen.route){
            CameraScreen()
        }
    }
}

sealed class Screen(val route: String){
    data object RewindScreen: Screen("rewind_screen")
    data object EntryScreen: Screen("entry_screen")
    data object CameraScreen: Screen("camera_screen")


}