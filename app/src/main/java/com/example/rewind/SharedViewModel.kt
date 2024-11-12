package com.example.rewind

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.rewind.entry.SelectedBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel: ViewModel() {
    private val _selectedBitmaps = MutableStateFlow<List<SelectedBitmap>>(emptyList())
    val selectedBitmaps = _selectedBitmaps.asStateFlow()

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

    fun removeAll(){
        _selectedBitmaps.value = emptyList()
    }
}