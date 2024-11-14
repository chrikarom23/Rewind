package com.example.rewind.rewind

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.compose.RewindTheme
import com.example.rewind.R
import com.example.rewind.Screen
import com.example.rewind.SelectedBitmap
import com.example.rewind.entry.savePhotos
import com.example.rewind.room.DayEntry
import java.time.LocalDate


@Composable
fun DetailDayEntry(
    modifier: Modifier = Modifier,
    dayEntry: DayEntry,
    updateDayEntry: (String, Int, List<String>) -> Boolean,
    selectedPhotos: List<SelectedBitmap>,
    dismissDialog: () -> Unit = {},
    navigateTo: (String) -> Unit,
    editState: Boolean,
    turnOnEdits: () -> Unit,
    turnOffEdits: () -> Unit,
    editable: Boolean
) {
    var description by remember { mutableStateOf(dayEntry.description) }
    var dayRating by remember { mutableIntStateOf(dayEntry.dayRating) }
    var dayPhotosURIs by remember { mutableStateOf(dayEntry.imageURIS) }

    var loading by remember{ mutableStateOf(false) }

    fun updateDayRating(newRating: Int){
        dayRating = newRating
    }

    fun updateDayPhotosURIs(bitmapURI: String){
        if(!dayPhotosURIs.contains(bitmapURI))dayPhotosURIs += bitmapURI
    }

    fun saveCurrentChanges(context: Context){
        loading = true
        println(selectedPhotos)
        println(dayPhotosURIs)
        dayPhotosURIs = emptyList()
        savePhotos(selectedPhotos,context,::updateDayPhotosURIs)
        updateDayEntry(description, dayRating,dayPhotosURIs)
        loading = false
        turnOffEdits()
        dismissDialog()
    }

    Surface(
        modifier = Modifier
            .padding(vertical = 20.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box{
            if(loading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    DetailDescriptionHeading(
                        dayEntry = dayEntry,
                        updatedDayRating = dayRating,
                        updateDayRating = ::updateDayRating,
                        edit = editState
                    )
                }
                item {
                    TextField(
                        modifier = Modifier
                            .padding(16.dp)
                            .wrapContentHeight(),
                        value = description,
                        onValueChange = { newDescription -> description = newDescription },
                        enabled = editState,
                        readOnly = !editState,
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = Color.Transparent,
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = MaterialTheme.shapes.medium,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                }
                item {
                    EditRewindPhotoCarousel(
                        edit = editState,
                        selectedPhotos = selectedPhotos,
                        navigateTo = navigateTo,
                        changeEditState = turnOnEdits,
                        editable = editable
                    )
                }
                item {
                    if(editable) EditRewindButton(edit = editState, changeEditState = turnOnEdits, saveChanges = ::saveCurrentChanges)
                }
            }
        }
    }
}

@Composable
fun EditRewindButton(
    modifier: Modifier = Modifier,
    edit: Boolean,
    changeEditState: () -> Unit,
    saveChanges: (Context) -> Unit
) {
        val context = LocalContext.current
        AnimatedVisibility(visible = !edit) {
            FilledTonalButton(
                onClick = { changeEditState() },
                modifier = Modifier.padding(horizontal = 6.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.elevatedButtonElevation(),
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Row {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit entry",
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 2.dp)
                    )
                }
            }
        }
        AnimatedVisibility(visible = edit) {
            FilledTonalButton(
                onClick = { saveChanges(context) },
                modifier = Modifier.padding(horizontal = 6.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.elevatedButtonElevation(),
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
            Row {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save entry",
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun EditRewindPhotoCarousel(
    modifier: Modifier = Modifier,
    edit: Boolean,
    selectedPhotos: List<SelectedBitmap>,
    navigateTo: (String) -> Unit,
    changeEditState: () -> Unit,
    editable: Boolean
) {
        LazyRow(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 4.dp)
        ) {
            items(selectedPhotos) { item ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .height(280.dp)
                        .padding(end = 4.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    shadowElevation = 2.dp
                ) {
                    Box {
                        Image(
                            bitmap = item.bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillHeight
                        )
                        AnimatedVisibility(visible = edit, enter = fadeIn(animationSpec = tween(durationMillis = 250)), exit = fadeOut(animationSpec = tween(durationMillis = 250))) {
                            Checkbox(modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(0.dp),
                                checked = item.isSelected.value,
                                onCheckedChange = { item.isSelected.value = !item.isSelected.value })
                        }
                    }

                }
            }
            item {
                if (editable)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .height(280.dp)
                        .width(200.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navigateTo(Screen.CameraScreen.route); changeEditState()}) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add photos",
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                    }
                }
            }
        }
//    }
}

@Composable
fun DetailDescriptionHeading(
    modifier: Modifier = Modifier,
    dayEntry: DayEntry,
    updatedDayRating: Int,
    updateDayRating: (Int) -> Unit,
    edit: Boolean,
) {
    var dropDownState by remember { mutableStateOf(false) }
    val editColorChange by animateColorAsState(
        if (edit) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "Edit color change"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp, end = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)
        ) {
            Text(
                text = dayEntry.dayWord,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth(),
                lineHeight = 26.sp
            )
            Text(
                text = dayEntry.date,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .fillMaxWidth(),
                lineHeight = 16.sp,
                fontStyle = FontStyle.Italic
            )
        }
        Column(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .clip(CircleShape)
                .background(if (!dropDownState) editColorChange else MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            GetRatingIcon(
                dayRating = updatedDayRating, modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = edit) { dropDownState = !dropDownState }
            )
            DropdownMenu(
                expanded = dropDownState,
                onDismissRequest = { dropDownState = !dropDownState },
                modifier = Modifier.size(60.dp, 65.dp),
                offset = DpOffset.Zero.copy(x = (-40).dp, y = (-10).dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                DropdownMenuItem(text = {
                    Icon(
                        painter = painterResource(id = R.drawable.pleased),
                        contentDescription = null
                    )
                }, onClick = { updateDayRating(5) })
                DropdownMenuItem(text = {
                    Icon(
                        painter = painterResource(id = R.drawable.smile),
                        contentDescription = null
                    )
                }, onClick = { updateDayRating(4) })
                DropdownMenuItem(text = {
                    Icon(
                        painter = painterResource(id = R.drawable.neutral),
                        contentDescription = null
                    )
                }, onClick = { updateDayRating(3) })
                DropdownMenuItem(text = {
                    Icon(
                        painter = painterResource(id = R.drawable.sad),
                        contentDescription = null
                    )
                }, onClick = { updateDayRating(2) })
                DropdownMenuItem(text = {
                    Icon(
                        painter = painterResource(id = R.drawable.angry),
                        contentDescription = null
                    )
                }, onClick = { updateDayRating(1) })
            }
        }

    }
}

fun loadAll(bitmapURIS: List<String>, context: Context, addPhoto: (Bitmap) -> Unit, addURI: (String) ->Unit){
    for(uri in bitmapURIS){
            loadPhoto(uri, context)?.let { addPhoto(it) }
            addURI(uri)
    }
}


@Preview(showBackground = true)
@Composable
private fun DetailDayEntryPreview() {
    RewindTheme {
        Column(Modifier.fillMaxSize()) {
            Dialog(onDismissRequest = {}) {
                DetailDayEntry(
                    dayEntry = DayEntry("Monday", "1/1/2001", "Good ig", 4, listOf()),
                    updateDayEntry = {i1,i2,i3 -> false},
                    selectedPhotos = emptyList(),
                    navigateTo = {},
                    editState = false,
                    turnOnEdits = {},
                    editable = true,
                    turnOffEdits = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailDayEntryNightPreview() {
    RewindTheme {
        Column(Modifier.fillMaxSize()) {
            Dialog(onDismissRequest = {}) {
                DetailDayEntry(
                    dayEntry = DayEntry("Monday", "1/1/2001", "Good ig", 4, listOf()),
                    updateDayEntry = {i1,i2,i3 -> false},
                    selectedPhotos = emptyList(),
                    navigateTo = {},
                    editState = false,
                    turnOnEdits = {},
                    turnOffEdits = {},
                    editable = true
                )
            }
        }
    }
}
