package com.example.rewind.rewind

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.rewind.room.DayEntry


@Composable
fun DetailDayEntry(
    modifier: Modifier = Modifier,
    dayEntry: DayEntry,
    updateDayEntry: (String, Int, List<String>) -> Boolean
) {
    var editState by rememberSaveable { mutableStateOf(false) }
    var updatedDescription by remember { mutableStateOf(dayEntry.description) }
    var updatedDayRating by remember { mutableIntStateOf(dayEntry.dayRating) }
    val updatedDayPhotoURISs by remember { mutableStateOf(dayEntry.imageURIS) }
    fun changeEditState() {
        editState = !editState
    }

    fun updateDayRating(newRating: Int) {
        updatedDayRating = newRating
    }

    Surface(
        modifier = Modifier
            .padding(vertical = 20.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                DetailDescriptionHeading(
                    dayEntry = dayEntry,
                    updatedDayRating = updatedDayRating,
                    updateDayRating = ::updateDayRating,
                    editState = editState)
            }
            item {
                TextField(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentHeight(),
                    value = updatedDescription,
                    onValueChange = { newDescription -> updatedDescription = newDescription },
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
                EditRewindPhotoCarousel(imageURIS = updatedDayPhotoURISs,editState = editState)
            }
            item {
                EditRewindButton(editState = editState, changeEditState = ::changeEditState)
            }
        }
    }
}

@Composable
fun EditRewindButton(
    modifier: Modifier = Modifier,
    editState: Boolean,
    changeEditState: () -> Unit
) {
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
        AnimatedVisibility(visible = !editState) {
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
        AnimatedVisibility(visible = editState) {
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
    imageURIS: List<String>,
    editState: Boolean
) {
    LazyRow(
        modifier = Modifier.fillMaxHeight(),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, bottom = 4.dp)
    ) {
        items(imageURIS) { uri ->
            val img = loadPhoto(uri, LocalContext.current)
            if (img != null) {
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
                    Image(
                        bitmap = img.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = editState, enter = fadeIn(), exit = fadeOut()) {
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
                        modifier = Modifier.clickable { }) {
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
    }
}

@Composable
fun DetailDescriptionHeading(
    modifier: Modifier = Modifier,
    dayEntry: DayEntry,
    updatedDayRating: Int,
    updateDayRating: (Int) -> Unit,
    editState: Boolean,
) {
    var dropDownState by remember { mutableStateOf(false) }
    val editColorChange by animateColorAsState(
        if(editState) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, label = "Edit color change"
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
                    .clickable(enabled = editState) { dropDownState = !dropDownState }
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


@Preview(showBackground = true)
@Composable
private fun DetailDayEntryPreview() {
    RewindTheme {
        Column(Modifier.fillMaxSize()) {
            Dialog(onDismissRequest = {}) {
                DetailDayEntry(
                    dayEntry = DayEntry("Monday", "1/1/2001", "Good ig", 4, listOf()),
                    updateDayEntry = { i1, i2, i3 -> false })
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
                    updateDayEntry = { i1, i2, i3 -> false })
            }
        }
    }
}
