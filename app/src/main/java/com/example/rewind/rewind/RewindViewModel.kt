package com.example.rewind.rewind

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.rewind.entry.SelectedBitmap
import com.example.rewind.repository.Repository
import com.example.rewind.room.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RewindViewModel(application: Application) : AndroidViewModel(application) {
//    private val _selectedBitmaps = MutableStateFlow<List<SelectedBitmap>>(emptyList())
//    val selectedBitmaps = _selectedBitmaps.asStateFlow()
//
//    private val description = mutableStateOf("")
//    val description = derivedStateOf { this.description.value }
//
//    private val _dayRating = MutableStateFlow<Int>(0)
//    val dayRating = _dayRating
//
//    private val _bitmapsURI = MutableStateFlow<List<String>>(emptyList())
//    val bitmapsURI = _bitmapsURI.asStateFlow()
//
    private val database = getDatabase(application)
    private val repository = Repository(database)

    private val viewModelJob= Job()
    private val coroutineScope = CoroutineScope(viewModelJob+ Dispatchers.IO)

    var dataURIs = repository.daysGoneBy

    fun getRandomDay(){
        coroutineScope.launch {
            Log.i("RewindViewModel", repository.getRandomDay().dayWord)
        }
    }

//    fun addPhoto(bitmap: Bitmap){
//        _selectedBitmaps.value += SelectedBitmap(bitmap)
//        Log.i("DayEntryViewModel", "bitmaps: ${_selectedBitmaps.value}")
//    }
//
//    fun updateDescription(description: String){
//        this.description.value = description
//    }
//
//    fun setRating(rating: Int){
//        _dayRating.value = rating
//    }
//
//    fun addURI(bitmapURI: String){
//        _bitmapsURI.value += bitmapURI
//    }

    fun saveToDB(descriptionP:String, dayRating: Int, bitmapsURI: List<String>): Boolean{
        var description = descriptionP
        var result = false;
        if(description.trim()==""){
            description = when(dayRating){
                1-> "To better days \uD83E\uDD42..."
                2-> "To better days \uD83E\uDD42..."
                3-> "Yet another day passed by..."
                4-> "It was a good day..."
                5-> "It was a great day..."
                else -> "Everything was alright \uD83D\uDE0C..."
            }
        }
        coroutineScope.launch {
            result = repository.saveToDatabase(bitmapsURI, description, dayRating)
        }
        return result
    }
}