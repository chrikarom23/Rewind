package com.example.rewind.entry

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.RewindTheme
import com.example.rewind.R
import com.example.rewind.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate


@Composable
fun DayEntryPermissionCheck(modifier: Modifier = Modifier) {
    var permissionState by remember{ mutableStateOf(false) }
    val cameraPermissionRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            isGranted ->
            permissionState = isGranted
        })
    SideEffect {
        cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
    }
    if(permissionState){
        Log.i("Permission Check", "Got permission")
    }
}

@Composable
fun DayEntry(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: DayEntryViewModel = viewModel()
) {
    //Should try using navbackstack instead. viewModel created in the activities scope persists even after navigating back from the first call.
    DayEntryScreen(
        navController = navController,
        bitmaps = viewModel.bitmaps,
        rating = viewModel::setRating,
        saveFunction = viewModel::saveToDB,
        addURI = viewModel::addURI,
        updateDescription = viewModel::updateDescription,
        description = viewModel.description
    )

}

@Composable
fun DayEntryScreen(modifier: Modifier = Modifier, navController: NavHostController, bitmaps: StateFlow<List<Bitmap>>, rating: (Int) -> Unit, saveFunction: () -> Boolean, addURI: (String)-> Unit, updateDescription: (String) -> Unit, description: State<String>) {
    DayEntryPermissionCheck()
    Scaffold (
        bottomBar = { BottomBarEntry(navController = navController, bitmapsStateFlow = bitmaps, saveFunction = saveFunction, saveBitmapURI = addURI)},
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ){
            padding ->
        Surface (
            modifier = Modifier
                .padding(padding)
                .padding(10.dp),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 10.dp){
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween) {
                FeelValue(
                    Modifier
                        .padding(4.dp)
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    rating = rating)
                DayDescriptionValue(
                    Modifier
                        .padding(4.dp)
                        .padding(horizontal = 8.dp),
                    updateDescription = updateDescription,
                    currentText = description)
                ExternalPhotoRequest(
                    Modifier
                        .padding(4.dp)
                        .padding(start = 8.dp))
                TakenPicturesCarousel(
                    Modifier
                        .padding(4.dp)
                        .padding(bottom = 4.dp),
                    navController = navController,
                    bitmapListStateFlow = bitmaps)
            }
        }
    }
}

@Composable
fun TakenPicturesCarousel(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    bitmapListStateFlow: StateFlow<List<Bitmap>>
) {
    val bitmaps by bitmapListStateFlow.collectAsState()
    Column (horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()){
        FilledTonalButton(onClick = { ;navController.navigate(Screen.CameraScreen.route) }) {
            Text(text = "Take some pictures ", style = MaterialTheme.typography.labelLarge)
            Icon(imageVector = Icons.Default.Camera, contentDescription = "Take a picture")
        }
    }
    if(bitmaps.isNotEmpty()){
        LazyRow(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top=0.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(200.dp)){
            items(bitmaps){
                    item ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(vertical = 6.dp)) {
                    Image(
                        bitmap = item.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
    else{
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
    Column(modifier = modifier){
        Text(
            text = "Describe your day",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = {updateDescription(it)},
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            supportingText = { Text("Try to keep it short and sweet :)") }
        )
    }
}

@Composable
fun setButtonDp(buttonValue: Int, clickedButtonState: Int): Dp {
    return animateDpAsState(targetValue = if(buttonValue==clickedButtonState) 50.dp else 14.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ).value
}
@Composable
fun FeelValue(modifier: Modifier = Modifier, rating: (Int) -> Unit) {
    Column(modifier) {
        var clickedButton by rememberSaveable{ mutableIntStateOf(-1) }

        Text(text = "How did you feel?",style = MaterialTheme.typography.headlineSmall, modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(67.dp),
            horizontalArrangement = Arrangement.SpaceBetween){
            FilledTonalButton(shape = RoundedCornerShape(setButtonDp(buttonValue = 1, clickedButtonState = clickedButton)), onClick = {rating(1);clickedButton=1}, modifier = Modifier
                .weight(0.2f)
                .size(100.dp)
                .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF000033))) {
                Icon(painter = painterResource(id = R.drawable.angry), contentDescription = "Very bad")
            }
            FilledTonalButton(shape = RoundedCornerShape(setButtonDp(buttonValue = 2, clickedButtonState = clickedButton)),
                onClick = {rating(2); clickedButton=2}, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF003311))) {
                Icon(painter = painterResource(id = R.drawable.sad), contentDescription = "Bad")
            }
            FilledTonalButton(shape = RoundedCornerShape(setButtonDp(buttonValue = 3, clickedButtonState = clickedButton)),
                onClick = {rating(3);clickedButton=3}, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF006633))) {
                Icon(painter = painterResource(id = R.drawable.neutral), contentDescription = "Neutral")
            }
            FilledTonalButton(shape = RoundedCornerShape(setButtonDp(buttonValue = 4, clickedButtonState = clickedButton)),
                onClick = {rating(4);clickedButton=4}, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF009933))) {
                Icon(painter = painterResource(id = R.drawable.smile), contentDescription = "Good")
            }
            FilledTonalButton(shape = RoundedCornerShape(setButtonDp(buttonValue = 5, clickedButtonState = clickedButton)),
                onClick = {rating(5);clickedButton=5}, modifier = Modifier
                    .weight(0.2f)
                    .size(100.dp)
                    .padding(start = 2.dp),
                colors = ButtonDefaults.buttonColors().copy(Color(0xFF00CC33))) {
                Icon(painter = painterResource(id = R.drawable.pleased), contentDescription = "Very good")
            }
        }
    }
}

@Composable
fun ExternalPhotoRequest(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(text = "Do you want to include photos you have taken today from other apps?",
            Modifier
                .fillMaxWidth()
                .weight(0.86f),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colorScheme.error)
        Switch(checked = false, onCheckedChange = {}, enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .weight(0.2f))
    }
}

@Composable
fun BottomBarEntry(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    bitmapsStateFlow: StateFlow<List<Bitmap>>,
    saveFunction: () -> Boolean,
    saveBitmapURI: (String) -> Unit
) {
    NavigationBar {
        val context = LocalContext.current.applicationContext
        val bitmaps by bitmapsStateFlow.collectAsState()
        Row (horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()){
            ElevatedButton(onClick = {
                savePhoto(bitmaps = bitmaps, context = context, saveBitmapURI = saveBitmapURI);
                if(saveFunction()){
                    Toast.makeText(context,"Successfully saved your entry!", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(context,"You've Already made an entry today!!!", Toast.LENGTH_LONG).show()
                }
                navController.navigate(Screen.RewindScreen.route)}) {
                Icon(imageVector = Icons.Default.AddTask, contentDescription = "Add Entry", Modifier.size(35.dp))
            }
        }
    }
}

fun savePhoto(bitmaps: List<Bitmap>, context: Context, saveBitmapURI: (String) -> Unit) {
    val date = LocalDate.now().toString()
    for(i in 1..bitmaps.size){
        context.openFileOutput("${date}_$i.jpg",MODE_PRIVATE).use{
            stream ->
            if(!bitmaps[i-1].compress(Bitmap.CompressFormat.JPEG, 70, stream)){
                    Log.e("Save Photo","Couldn't write to file")
            }
            else{
                saveBitmapURI("${date}_$i.jpg")
            }
        }
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

