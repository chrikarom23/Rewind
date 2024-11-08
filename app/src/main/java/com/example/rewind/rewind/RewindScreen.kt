package com.example.rewind.rewind

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.RewindTheme
import com.example.rewind.R
import com.example.rewind.Screen
import com.example.rewind.room.DayEntry
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RewindScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    rewindViewModel: RewindViewModel = viewModel(),
) {
    val daysGoneBy by rewindViewModel.dataURIs.observeAsState(initial = emptyList())
    val lazyListState = rememberLazyListState()
    val canScrollForward = lazyListState.canScrollForward
    var dayEntry by remember { mutableStateOf<DayEntry?>(null) }
    val haptics = LocalHapticFeedback.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = { AddDayEntry(navController = navController) },
        topBar = { TopBar(getRandomDay = rewindViewModel::getRandomDay) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (dayEntry != null) {
            Dialog(onDismissRequest = { dayEntry = null }) {
                DetailDayEntry(dayEntry = dayEntry!!, updateDayEntry = rewindViewModel::saveToDB)
            }
        }
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr) + 4.dp,
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr) + 4.dp,
                top = innerPadding.calculateTopPadding() - 2.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp
            ),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = TopWithLogo,
        ) {
            items(daysGoneBy) { item ->
                DayItem(
                    item = item,
                    modifier = Modifier.combinedClickable(enabled = true, onLongClick = {
                        haptics.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        ); dayEntry = item
                    }, onClick = {})
                )
            }
            item {
                LazyListFooter(cannotScrollForward = canScrollForward)
            }
        }
    }
}

object TopWithLogo : Arrangement.Vertical {
    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        var y = 0
        sizes.forEachIndexed { index, size ->
            outPositions[index] = y
            y += size
        }

        outPositions[outPositions.lastIndex] = totalSize - sizes.last()
    }
}

@Composable
fun LazyListFooter(modifier: Modifier = Modifier, cannotScrollForward: Boolean) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp)
            .alpha(0.2f)
            .blur(4.dp),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
//        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = !cannotScrollForward,
                modifier = Modifier.height(200.dp),
                enter = fadeIn(animationSpec = tween(durationMillis = 700)),
                exit = fadeOut(animationSpec = tween(durationMillis = 700))
            ) {
                Text(
                    text = "Rewind",
                    modifier = Modifier
                        .padding(10.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
                    style = MaterialTheme.typography.displayLarge,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun DayItem(modifier: Modifier = Modifier, item: DayEntry) {
    Surface(
        border = BorderStroke(
            1.dp,
            if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent
        ),
        modifier = modifier
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            DayDescription(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .padding(horizontal = 8.dp)
                    .fillMaxSize()
                    .weight(0.65f),
                dayWord = item.dayWord,
                dayRating = item.dayRating,
                date = item.date,
                description = item.description
            )
            if (item.imageURIS.isNotEmpty()) {
                DayPics(
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(0.35f),
                    imageURIs = item.imageURIS
                )
            }
        }
    }
}


@Composable
fun DayDescription(
    modifier: Modifier = Modifier,
    dayWord: String,
    dayRating: Int,
    date: String,
    description: String
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            ) {
                Text(
                    text = dayWord, style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth(),
                    lineHeight = 26.sp,

                    )
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .fillMaxWidth(),
                    lineHeight = 16.sp,
                    fontStyle = FontStyle.Italic
                )
            }
            GetRatingIcon(
                dayRating = dayRating, modifier = Modifier
                    .padding(top = 2.dp)
                    .weight(0.15f)
                    .fillMaxWidth()
                    .align(Alignment.Top)
            )
        }
        Text(
            text = description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
                .wrapContentHeight()
                .wrapContentWidth(),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GetRatingIcon(modifier: Modifier = Modifier, dayRating: Int) {
    var temp by remember {
        mutableIntStateOf(dayRating)
    }
    temp = dayRating
    val ico by remember {
        derivedStateOf {
            when (temp) {
                1 -> R.drawable.angry
                2 -> R.drawable.sad
                3 -> R.drawable.neutral
                4 -> R.drawable.smile
                5 -> R.drawable.pleased
                else -> R.drawable.ic_launcher_foreground
            }
        }
    }
    Column(modifier = modifier) {
        Icon(
            painter = painterResource(id = ico), contentDescription = "FavoriteIcon",
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun DayPics(modifier: Modifier = Modifier, imageURIs: List<String>) {
    val context = LocalContext.current

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 6.dp)) {
        items(imageURIs) { uri ->
            val img = loadPhoto(uri, context)
            if (img != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Image(
                        bitmap = img.asImageBitmap(),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

fun loadPhoto(imageURI: String, context: Context): Bitmap? {
    try {
        val files = context.filesDir.listFiles()
        files?.filter { it.canRead() && it.isFile && it.name == imageURI }?.map {
            val bytes = it.readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            return bmp
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("RewindScreen", "Ran into an error while trying to load photos")
        return null
    }
    return null
}

@Composable
fun AddDayEntry(modifier: Modifier = Modifier, navController: NavHostController) {
    FloatingActionButton(
        onClick = {
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
fun TopBar(modifier: Modifier = Modifier, getRandomDay: () -> Unit) {
    ElevatedButton(
        onClick = { getRandomDay() },
        modifier = Modifier
            .padding(start = 8.dp, top = 38.dp),
        shape = CircleShape,
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RewindTheme {
        val navController = rememberNavController()
        RewindScreenPreview(navController = navController)
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreviewNight() {
    RewindTheme {
        val navController = rememberNavController()
        RewindScreenPreview(navController = navController)
    }
}


@Composable
fun RewindScreenPreview(modifier: Modifier = Modifier, navController: NavHostController) {
    val dayData = mutableListOf(
        DayEntry("Monday", "1/1/2001", "Good ig", 4, listOf()),
        DayEntry("Tuesday", "1/1/2001", "Good ig", -1, listOf()),
        DayEntry("Wednesday", "1/1/2001", "Good ig", 3, listOf()),
        DayEntry("Thursday", "1/1/2001", "Good ig", 4, listOf()),
        DayEntry("Friday", "1/1/2001", "Good ig", 4, listOf()),
        DayEntry("Saturday", "1/1/2001", "Good ig", 4, listOf()),
        DayEntry("Sunday", "1/1/2001", "Good ig", 4, listOf())
    )

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color.Transparent.copy(0.3f))

    Scaffold(
        Modifier.fillMaxSize(),
        floatingActionButton = { AddDayEntry(navController = navController) },
        topBar = { TopBar(getRandomDay = { }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                top = innerPadding.calculateTopPadding() - 2.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp
            )
        ) {
            items(dayData) { item ->
                DayItem(modifier, item)
            }
            item {
                LazyListFooter(cannotScrollForward = false)
            }
        }
    }
}
