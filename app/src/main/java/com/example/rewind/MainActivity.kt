package com.example.rewind

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.compose.RewindTheme
import com.example.rewind.entry.DayEntry

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RewindTheme {
                Navigation()
            }
        }
    }
}

const val PREFS_NAME = "rewind_time_preferences"
const val SAMPLE_KEY = "last_update_date"

fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

fun saveData(context: Context, value: String) {
    val sharedPreferences = getSharedPreferences(context)
    with(sharedPreferences.edit()) {
        putString(SAMPLE_KEY, value)
        apply()
    }
}

fun getData(context: Context): String? {
    val sharedPreferences = getSharedPreferences(context)
    return sharedPreferences.getString(SAMPLE_KEY, null)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RewindTheme {
        Navigation()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreviewNight() {
    RewindTheme {
        Navigation()
    }
}

@Preview(showBackground = true)
@Composable
fun DayEntryPreview() {
    RewindTheme {
        val navController = rememberNavController()
        DayEntry(navController = navController)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DayEntryPreviewNight() {
    RewindTheme {
        val navController = rememberNavController()
        DayEntry(navController = navController)
    }
}
