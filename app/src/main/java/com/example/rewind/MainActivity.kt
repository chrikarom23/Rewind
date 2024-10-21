package com.example.rewind

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
