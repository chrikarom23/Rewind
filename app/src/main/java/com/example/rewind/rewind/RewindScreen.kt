package com.example.rewind.rewind

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.RewindTheme
import com.example.rewind.MainViewModel
import com.example.rewind.R
import com.example.rewind.Screen
import com.example.rewind.domain.Day
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun RewindScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    navController: NavHostController,
) {
    val systemUiController = rememberSystemUiController()

//    SideEffect {
//        systemUiController.isNavigationBarContrastEnforced = true
//        systemUiController.isSystemBarsVisible = false
//    }

    systemUiController.setSystemBarsColor(color = Color.Transparent.copy(0.3f))

    Scaffold(
        Modifier.fillMaxSize(),
        floatingActionButton = { AddDayEntry(navController = navController) },
        topBar = { TopBar() },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ){
            innerPadding ->
        LazyColumn (
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                top = innerPadding.calculateTopPadding()-2.dp,
                bottom = innerPadding.calculateBottomPadding()+8.dp
            )
        ){
            items(mainViewModel.daysData.value!!){
                    item ->
                DayItem(modifier, item)
            }
        }
    }
}

@Composable
fun DayItem(modifier: Modifier = Modifier, item: Day) {

    Surface(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 3.dp,
//        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            DayDescription(
                modifier = modifier
                    .padding(vertical = 4.dp)
                    .padding(start = 8.dp)
                    .fillMaxWidth()
                    .weight(0.65f),
                item
            )
            DayPics(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            )
        }
    }
}

@Composable
fun DayDescription(modifier: Modifier = Modifier, item: Day) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 10.dp)){
            Text(text = item.dayWord, style = MaterialTheme.typography.headlineMedium, modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f))
            Icon(imageVector = Icons.Default.FavoriteBorder,contentDescription = "FavoriteIcon",
                modifier = Modifier
                    .weight(0.1f)
                    .size(25.dp))
        }
        Text(text = item.date.toString(), style = MaterialTheme.typography.labelMedium, modifier = Modifier
            .padding(start = 2.dp)
            .fillMaxWidth(), fontStyle = FontStyle.Italic)
        Text(text = item.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                ScrollState(0)
            )
            .padding(start = 2.dp)
            .wrapContentHeight()
            ,
            textAlign = TextAlign.Center)
        //Check scroll state
    }
}

@Composable
fun DayPics(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier){
        items(testData){
                item ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .padding(horizontal = 6.dp)){
                Image(
                    painter = painterResource(id = item),
                    contentDescription = null)
            }
        }
    }
}

private val testData = listOf(
    R.drawable.dummy1,
    R.drawable.dummy2,
    R.drawable.dummy3,
    R.drawable.dummy1,
    R.drawable.dummy2,
    R.drawable.dummy3,
    R.drawable.dummy1,
    R.drawable.dummy2,
    R.drawable.dummy3
).shuffled()

@Composable
fun AddDayEntry(modifier: Modifier = Modifier, navController: NavHostController) {
    FloatingActionButton(
        onClick = {
            Log.i("MainScreen", "Must goto day add page")
            navController.navigate(Screen.EntryScreen.route)
        },
        modifier = Modifier.size(50.dp),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add day entry")
    }
}

@Composable
fun TopBar(modifier: Modifier = Modifier) {
    ElevatedButton(
        onClick = {},
        modifier = Modifier
            .padding(start = 8.dp, top = 38.dp),
        shape = CircleShape,
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
    ) {
        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(25.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RewindTheme {
        val navController = rememberNavController()
        RewindScreen(navController = navController)
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreviewNight() {
    RewindTheme {
        val navController = rememberNavController()
        RewindScreen(navController = navController)
    }
}