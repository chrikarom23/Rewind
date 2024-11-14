package com.example.rewind

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rewind.camera.CameraScreen
import com.example.rewind.entry.DayEntry
import com.example.rewind.entry.DayEntryViewModel
import com.example.rewind.rewind.RewindScreen
import com.example.rewind.rewind.RewindViewModel

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val activity = LocalContext.current
    NavHost(navController = navController, startDestination = Screen.RewindScreen.route){
        composable(route = Screen.RewindScreen.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }, popEnterTransition = { fadeIn() }, popExitTransition = { fadeOut() }) {
            val rewindViewModel: RewindViewModel = viewModel(activity as ViewModelStoreOwner)
            val sharedViewModel: SharedViewModel = viewModel(activity as ViewModelStoreOwner)
            RewindScreen (navController = navController, rewindViewModel = rewindViewModel,sharedViewModel = sharedViewModel)
        }

        composable(route = Screen.EntryScreen.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }, popEnterTransition = { fadeIn() }, popExitTransition = { fadeOut() }) {
            val dayEntryViewModel: DayEntryViewModel = viewModel(activity as ViewModelStoreOwner)
            val sharedViewModel: SharedViewModel = viewModel(activity as ViewModelStoreOwner)
            DayEntry (navController = navController, viewModel = dayEntryViewModel, sharedViewModel = sharedViewModel)
        }

        composable(route = Screen.CameraScreen.route, enterTransition = { fadeIn() }, exitTransition = { fadeOut() }, popEnterTransition = { fadeIn() }, popExitTransition = { fadeOut() }){
            val sharedViewModel: SharedViewModel = viewModel(activity as ViewModelStoreOwner)
            CameraScreen(sharedViewModel = sharedViewModel)
        }
    }
}

sealed class Screen(val route: String){
    data object RewindScreen: Screen("rewind_screen")
    data object EntryScreen: Screen("entry_screen")
    data object CameraScreen: Screen("camera_screen")
}