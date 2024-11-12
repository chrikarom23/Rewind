package com.example.rewind.entry

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.RewindTheme
import com.example.rewind.R
import com.example.rewind.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rewind.SharedViewModel
import com.example.rewind.saveData
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDate


@Composable
fun DayEntryPermissionCheck(modifier: Modifier = Modifier, cameraStateEvent: (Boolean) -> Unit) {
    var permissionState by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionResult = rememberSaveable {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    }
    if (permissionResult == PackageManager.PERMISSION_DENIED) {
        if(!permissionState){
            AlertDialog(
                onDismissRequest = {
                    permissionState = true
                },
                confirmButton = {
                    Button(onClick = { permissionState = true }) {
                        Text(text = "Allow Permission")
                    }
                },
                title = { Text(text = "Camera Permission Needed") },
                text = { Text(text = "Need Camera Permission to capture and add photos. You can refuse but your experience will be degraded.") }
            )
        }
        else{
            RequestRewindPermission(permissionStateEvent = cameraStateEvent)
        }
    } else {
        Log.i("DayEntryScreen", "Camera permissions have been granted")
        cameraStateEvent(true)
    }
}

@Composable
fun RequestRewindPermission(
    modifier: Modifier = Modifier,
    permissionStateEvent: (Boolean) -> Unit
) {
    val cameraPermissionRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            permissionStateEvent(isGranted)
        })
    SideEffect {
        cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
    }
}

@Composable
fun DayEntry(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: DayEntryViewModel = viewModel(),
    sharedViewModel: SharedViewModel = viewModel()
) {
    //Should try using navbackstack instead. viewModel created in the activities scope persists even after navigating back from the first call.

    DayEntryScreen(
        navController = navController,
        bitmaps = sharedViewModel.selectedBitmaps,
        rating = viewModel::setRating,
        saveFunction = viewModel::saveToDB,
        addURI = viewModel::addURI,
        updateDescription = viewModel::updateDescription,
        description = viewModel.description
    )

}

@Composable
fun DayEntryScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    bitmaps: StateFlow<List<SelectedBitmap>>,
    rating: (Int) -> Unit,
    saveFunction: () -> Boolean,
    addURI: (String) -> Unit,
    updateDescription: (String) -> Unit,
    description: State<String>
) {
    var cameraPermission by remember { mutableStateOf(false) }
    DayEntryPermissionCheck(cameraStateEvent = {isGranted -> cameraPermission=isGranted})
    Scaffold(
        bottomBar = {
            BottomBarEntry(
                navController = navController,
                bitmapsStateFlow = bitmaps,
                saveFunction = saveFunction,
                saveBitmapURI = addURI
            )
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) { padding ->
        Surface(
            modifier = Modifier
                .padding(padding)
                .padding(10.dp),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 10.dp
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                FeelValue(
                    Modifier
                        .padding(4.dp)
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    rating = rating
                )
                DayDescriptionValue(
                    Modifier
                        .padding(4.dp)
                        .padding(horizontal = 8.dp),
                    updateDescription = updateDescription,
                    currentText = description
                )
                ExternalPhotoRequest(
                    Modifier
                        .padding(4.dp)
                        .padding(start = 8.dp)
                )
                TakenPicturesCarousel(
                    Modifier
                        .padding(4.dp)
                        .padding(bottom = 4.dp),
                    navController = navController,
                    bitmapListStateFlow = bitmaps,
                    cameraState = cameraPermission
                )
            }
        }
    }
}

@Composable
fun TakenPicturesCarousel(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    bitmapListStateFlow: StateFlow<List<SelectedBitmap>>,
    cameraState: Boolean
) {
    val bitmaps by bitmapListStateFlow.collectAsState()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        FilledTonalButton(onClick = { navController.navigate(Screen.CameraScreen.route) }, enabled = cameraState) {
            Text(text = "Take some pictures ", style = MaterialTheme.typography.labelLarge)
            Icon(imageVector = Icons.Default.Camera, contentDescription = "Take a picture")
        }
    }
    if (bitmaps.isNotEmpty()) {
        LazyRow(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(bitmaps) { item ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                ) {
                    Box{
                        Image(
                            bitmap = item.bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit
                        )
                        Checkbox(modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(0.dp), checked = item.isSelected.value, onCheckedChange = { item.isSelected.value = !item.isSelected.value})
                    }
                }
            }
        }
    } else {
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun DayDescriptionValue(
    modifier: Modifier = Modifier,
    updateDescription: (String) -> Unit,
    currentText: State<String>
) {
    val description by currentText
    Column(modifier = modifier) {
        Text(
            text = "Describe your day",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { updateDescription(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            supportingText = { Text("Try to keep it short and sweet :)") }
        )
    }
}

@Composable
fun setButtonDp(buttonValue: Int, clickedButtonState: Int): Dp {
    return animateDpAsState(
        targetValue = if (buttonValue == clickedButtonState) 50.dp else 14.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ).value
}

@Composable
fun FeelValue(modifier: Modifier = Modifier, rating: (Int) -> Unit) {
    Column(modifier) {
        var clickedButton by rememberSaveable { mutableIntStateOf(-1) }

        Text(
            text = "How did you feel?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(67.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(
                shape = RoundedCornerShape(
                    setButtonDp(
                        buttonValue = 1,
                        clickedButtonState = clickedButton
                    )
                ), onClick = { rating(1);clickedButton = 1 }, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF000033))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.angry),
                    contentDescription = "Very bad"
                )
            }
            FilledTonalButton(
                shape = RoundedCornerShape(
                    setButtonDp(
                        buttonValue = 2,
                        clickedButtonState = clickedButton
                    )
                ),
                onClick = { rating(2); clickedButton = 2 }, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF003311))
            ) {
                Icon(painter = painterResource(id = R.drawable.sad), contentDescription = "Bad")
            }
            FilledTonalButton(
                shape = RoundedCornerShape(
                    setButtonDp(
                        buttonValue = 3,
                        clickedButtonState = clickedButton
                    )
                ),
                onClick = { rating(3);clickedButton = 3 }, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF006633))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.neutral),
                    contentDescription = "Neutral"
                )
            }
            FilledTonalButton(
                shape = RoundedCornerShape(
                    setButtonDp(
                        buttonValue = 4,
                        clickedButtonState = clickedButton
                    )
                ),
                onClick = { rating(4);clickedButton = 4 }, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF009933))
            ) {
                Icon(painter = painterResource(id = R.drawable.smile), contentDescription = "Good")
            }
            FilledTonalButton(
                shape = RoundedCornerShape(
                    setButtonDp(
                        buttonValue = 5,
                        clickedButtonState = clickedButton
                    )
                ),
                onClick = { rating(5);clickedButton = 5 }, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF00CC33))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pleased),
                    contentDescription = "Very good"
                )
            }
        }
    }
}

@Composable
fun ExternalPhotoRequest(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            text = "Do you want to include photos you have taken today from other apps?",
            Modifier
                .fillMaxWidth()
                .weight(0.86f),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.error
        )
        Switch(
            checked = false, onCheckedChange = {}, enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .weight(0.2f)
        )
    }
}

@Composable
fun BottomBarEntry(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    bitmapsStateFlow: StateFlow<List<SelectedBitmap>>,
    saveFunction: () -> Boolean,
    saveBitmapURI: (String) -> Unit
) {
    NavigationBar {
        val context = LocalContext.current
        val bitmaps by bitmapsStateFlow.collectAsState()
        val date = LocalDate.now().toString()
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(onClick = {
                savePhotos(bitmaps = bitmaps, context = context, saveBitmapURI = saveBitmapURI)
                saveFunction()
                saveData(context, date)
                navController.navigate(Screen.RewindScreen.route)
            }) {
                Icon(
                    imageVector = Icons.Default.AddTask,
                    contentDescription = "Add Entry",
                    Modifier.size(35.dp)
                )
            }
        }
    }
}

fun savePhotos(bitmaps: List<SelectedBitmap>, context: Context, saveBitmapURI: (String) -> Unit) {
    bitmaps.forEach {
        val name = generateConsistentBitmapHash(it.bitmap)
        context.openFileOutput(name, MODE_PRIVATE).use { stream ->
                if(it.isSelected.value){
                    if (!it.bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)) {
                        Log.e("Save Photo", "Couldn't write to file")
                        context.deleteFile(name)
                    } else {
                        saveBitmapURI(name)
                    }
                }
                else{
                    context.deleteFile(name)
                }
        }
    }
//    for (i in 1..test.size) {
//
//    }
}

fun generateConsistentBitmapHash(bitmap: Bitmap): String {
    val buffer = ByteBuffer.allocate(bitmap.byteCount)
    bitmap.copyPixelsToBuffer(buffer)
    val bytes = buffer.array()
    val md = MessageDigest.getInstance("SHA-256")
    val hashBytes = md.digest(bytes)
    return hashBytes.joinToString("") { "%02x".format(it) } + ".jpg"
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

