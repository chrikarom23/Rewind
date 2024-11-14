package com.example.rewind.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rewind.SelectedBitmap
import com.example.rewind.SharedViewModel
import com.example.rewind.entry.DayEntryPermissionCheck
import com.example.rewind.entry.permissions
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

private var recording: Recording? = null

@Composable
fun CameraPreview(controller: LifecycleCameraController, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = {
        PreviewView(it).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
        }
    },
        modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    sharedViewModel: SharedViewModel = viewModel(),
) {
    var cameraPermission by remember { mutableStateOf(false) }
//    var recordAudioPermission by remember { mutableStateOf(false) }
    permissions.forEach {
        DayEntryPermissionCheck(
            permissionStateEvent = { isGranted -> cameraPermission = isGranted },
            permission = it
        )
    }
    val context = LocalContext.current
    val controller = remember{
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val bitmaps by sharedViewModel.selectedBitmaps.collectAsState()
    val scope = rememberCoroutineScope()
    BottomSheetScaffold(
        sheetContent = {
            PhotoBottomSheetContent(
                    bitmaps = bitmaps,
                    modifier = Modifier.fillMaxWidth())
        },
        sheetPeekHeight = 0.dp,
        scaffoldState = scaffoldState
        ) {
        innerPadding ->
        Surface (modifier= Modifier
            .padding(
                PaddingValues(
                    bottom = innerPadding.calculateBottomPadding() + 46.dp, top = innerPadding
                        .calculateTopPadding() + 30.dp
                )
            )
            .padding(10.dp),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 10.dp){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding()
            ) {
                CameraPreview(controller = controller, modifier.fillMaxSize())
                IconButton(
                    onClick = {
                        controller.cameraSelector =
                            if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            else CameraSelector.DEFAULT_BACK_CAMERA
                    },
                    modifier = Modifier
                        .padding(start = 16.dp, top = 6.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch camera"
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(shape = CircleShape)
                        .background(Color.Gray.copy(0.2f))
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround,

                    ){
                    IconButton(onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(0.2f))
                            .fillMaxWidth()
                            .weight(1f)) {
                        Icon(imageVector = Icons.Default.Photo,
                            contentDescription = "Open photos")
                    }
                    IconButton(onClick = {
                        takePhoto(controller = controller, onPhotoTaken = sharedViewModel::addPhoto ,context = context)
                    },
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(0.2f))
                            .fillMaxWidth()
                            .weight(1f)) {
                        Icon(imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take photo")
                    }
                    val recordingState by remember{ mutableStateOf(false) }
                    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
                    val recordingColor by infiniteTransition.animateColor(
                        initialValue = Color.Gray.copy(0.2f),
                        targetValue = Color.Red.copy(0.8f),
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "color"
                    )
//                    IconButton(onClick = {
//                        recordVideo(controller = controller, context = context, audioState = recordAudioPermission, sharedViewModel::addVideo)
//                    },
//                        modifier = Modifier
//                            .padding(4.dp)
//                            .clip(CircleShape)
//                            .background(color = if(recordingState) Color.Gray.copy(0.2f) else recordingColor)
//                            .fillMaxWidth()
//                            .weight(1f)) {
//                        Icon(imageVector = Icons.Default.Videocam,
//                            contentDescription = "Take video",)
//                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun PhotoBottomSheetContent(
    bitmaps: List<SelectedBitmap>,
    modifier: Modifier = Modifier
) {
    Column(modifier= modifier.padding(bottom = 100.dp)) {
        if(bitmaps.isEmpty()){
            Box(modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center){
                Text(text = "No photos yet", style = MaterialTheme.typography.labelLarge)
            }
        }
        else{
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp,
                contentPadding = PaddingValues(16.dp),
                modifier = modifier
            ) {
                items(bitmaps){ selectedBitmap ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))){
                        Image(
                            bitmap = selectedBitmap.bitmap.asImageBitmap(),
                            contentDescription = null)
                        Checkbox(modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(0.dp), checked = selectedBitmap.isSelected.value, onCheckedChange = { selectedBitmap.isSelected.value = !selectedBitmap.isSelected.value})
                    }
                }
            }
        }
    }
}

//@SuppressLint("MissingPermission")
//private fun recordVideo(controller: LifecycleCameraController, context: Context, audioState: Boolean, addVideo: (String) -> Unit){
//    if(recording!=null){
//        recording?.stop()
//        recording = null
//        return
//    }
//    val name = UUID.randomUUID().toString()
//    val outputFile = File(context.filesDir, "$name.mp4")
//    recording = controller.startRecording(
//        FileOutputOptions.Builder(outputFile).build(),
//        AudioConfig.create(audioState),
//        ContextCompat.getMainExecutor(context.applicationContext)
//    ){event ->
//        when(event){
//            is VideoRecordEvent.Finalize ->{
//                if(event.hasError()){
//                    recording?.close()
//                    recording = null
//
//                    Toast.makeText(context, "There was an error during recording", Toast.LENGTH_LONG).show()
//                    Log.i("VideoCaptureError", event.cause.toString())
//                }
//                else{
//                    addVideo("$name.mp4")
//                    Toast.makeText(context, "Video Captured!", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//}

private fun takePhoto(
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit,
    context: Context
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : OnImageCapturedCallback(){
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }

                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onPhotoTaken(rotatedBitmap)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera Error", "Coudlnt take photo due to error: ${exception.message}")
            }
        }
    )
}