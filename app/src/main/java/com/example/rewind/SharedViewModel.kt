package com.example.rewind

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel: ViewModel() {
    private val _selectedBitmaps = MutableStateFlow<List<SelectedBitmap>>(emptyList())
    val selectedBitmaps = _selectedBitmaps.asStateFlow()

    private val _selectedVideoURIs = MutableStateFlow<List<SelectedVideoURIs>>(emptyList())
    val selectedVideoURIs = _selectedVideoURIs.asStateFlow()

    private val _editState = MutableStateFlow<Boolean>(false)
    val editState = _editState.asStateFlow()

    fun turnOnEditing(){
        _editState.value = true
    }

    fun turnOffEditing(){
        _editState.value = false
    }

    fun addPhoto(bitmap: Bitmap){
        var exists = false
        for(i in selectedBitmaps.value){
            if(i.bitmap == bitmap) exists = true
        }
        if(!exists){
            _selectedBitmaps.value += SelectedBitmap(bitmap)
        }
    }

    fun addVideo(videoURI: String){
        var exists = false
        for(i in selectedVideoURIs.value){
            if(i.videoURI == videoURI) exists = true
        }
        if(!exists){
            _selectedVideoURIs.value += SelectedVideoURIs(videoURI)
        }
    }

    fun removeAll(){
        _selectedBitmaps.value = emptyList()
        _selectedVideoURIs.value = emptyList()
    }
}

data class SelectedBitmap(val bitmap: Bitmap, var isSelected: MutableState<Boolean> = mutableStateOf(true))

data class SelectedVideoURIs(val videoURI: String, var isSelected: MutableState<Boolean> = mutableStateOf(true))