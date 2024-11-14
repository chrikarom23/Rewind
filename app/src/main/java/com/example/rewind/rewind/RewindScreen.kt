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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
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
import com.example.rewind.SharedViewModel
import com.example.rewind.getData
import com.example.rewind.room.DayEntry
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RewindScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    rewindViewModel: RewindViewModel = viewModel(),
    sharedViewModel: SharedViewModel = viewModel(),
) {
    val lazyListState = rememberLazyListState()
    val canScrollForward by remember {
        derivedStateOf {
            lazyListState.canScrollForward
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val dayEntry by rewindViewModel.dayEntry.collectAsState() //remember { mutableStateOf<DayEntry?>(null) }
    val haptics = LocalHapticFeedback.current

    val daysGoneBy by rewindViewModel.dataURIs.observeAsState()
    val lastUpdated = getData(LocalContext.current)
    val editState by sharedViewModel.editState.collectAsState()

    val selectedBitmaps by sharedViewModel.selectedBitmaps.collectAsState()
    fun navigateTo(route: String) {
        navController.navigate(route)
    }

    fun goToRandomDay(){
        coroutineScope.launch {
            val elementCount = lazyListState.layoutInfo.totalItemsCount
            val random = Random.nextInt(until = elementCount-1)
            lazyListState.animateScrollToItem(random, scrollOffset = 0)
            rewindViewModel.setDayEntry(daysGoneBy?.get(random))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            AddDayEntry(
                navigateToDayEntry = ::navigateTo,
                lastUpdated = lastUpdated
            )
        },
        topBar = { TopBar(getRandomDay = ::goToRandomDay) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (dayEntry != null) {
            if(!editState){
                rewindViewModel.resetAll()
                sharedViewModel.removeAll()
                loadAll(
                    dayEntry!!.imageURIS,
                    LocalContext.current,
                    sharedViewModel::addPhoto,
                    rewindViewModel::addURI
                )
            }
            val editable by remember {
                derivedStateOf {
                    dayEntry!!.date == LocalDate.now().toString()
                }
            }
            Dialog(
                onDismissRequest = {
                    rewindViewModel.setDayEntry(null); sharedViewModel.removeAll(); sharedViewModel.turnOffEditing()
                }
            ) {
                DetailDayEntry(
                    dayEntry = dayEntry!!,
                    updateDayEntry = rewindViewModel::updateDayEntry,
                    selectedPhotos = selectedBitmaps,
                    navigateTo = ::navigateTo,
                    editState = editState,
                    turnOnEdits = sharedViewModel::turnOnEditing,
                    turnOffEdits = sharedViewModel::turnOffEditing,
                    dismissDialog = rewindViewModel::resetAll,
                    editable = editable
                )
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
            items(items = daysGoneBy?: emptyList(), key = {it.date}) { item ->
                DayItem(
                    item = item,
                    modifier = Modifier.combinedClickable(enabled = true, onLongClick = {
                        haptics.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                        rewindViewModel.setDayEntry(item)
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
                .height(200.dp)
                .padding(top = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = !cannotScrollForward,
                modifier = Modifier.height(200.dp),
                enter = fadeIn(animationSpec = tween(durationMillis = 600)),
                exit = fadeOut(animationSpec = tween(durationMillis = 600))
            ) {
                Row(modifier = Modifier
                    .padding(10.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically){
                    Text(
                        text = "Rewind",
                        style = MaterialTheme.typography.displayLarge,
                        fontStyle = FontStyle.Italic
                    )
                    Image(painter = painterResource(id = R.drawable.icon), contentDescription = "Rewind Logo", modifier = Modifier.size(70.dp).padding(start = 4.dp))
                }
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
                else -> R.drawable.icon
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
        items(items = imageURIs, key = {it}) { uri ->
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
fun AddDayEntry(
    modifier: Modifier = Modifier,
    navigateToDayEntry: (String) -> Unit,
    lastUpdated: String?
) {
    if (lastUpdated == null || lastUpdated != LocalDate.now().toString()) {
        FloatingActionButton(
            onClick = {
                navigateToDayEntry(Screen.EntryScreen.route)
            },
            modifier = Modifier.size(50.dp),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add day entry")
        }
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
        RewindScreenPreview()
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GreetingPreviewNight() {
    RewindTheme {
        val navController = rememberNavController()
        RewindScreenPreview()
    }
}


@Composable
fun RewindScreenPreview(modifier: Modifier = Modifier) {
    val dayData = mutableListOf(
        DayEntry("Monday", "1/1/2001", "Good ig", 4, listOf()),
        DayEntry("Tuesday", "1/1/2001", "Good ig", -1, listOf())
    )

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color.Transparent.copy(0.3f))

    Scaffold(
        Modifier.fillMaxSize(),
        floatingActionButton = {
            AddDayEntry(
                navigateToDayEntry = {},
                lastUpdated = LocalDate.now().toString()
            )
        },
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
