package com.example.rewind.entry

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.rewind.repository.Repository
import com.example.rewind.room.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DayEntryViewModel(application: Application) :AndroidViewModel(application) {

//    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
//    val bitmaps = _bitmaps.asStateFlow()
//
//    private val _selectedBitmaps = MutableStateFlow<List<SelectedBitmap>>(emptyList())
//    val selectedBitmaps = _selectedBitmaps.asStateFlow()

    private val _description = mutableStateOf("")
    val description = derivedStateOf { _description.value }

    private val _dayRating = MutableStateFlow<Int>(0)
    val dayRating = _dayRating

    private val _bitmapsURI = MutableStateFlow<List<String>>(emptyList())
    val bitmapsURI = _bitmapsURI.asStateFlow()

    private val database = getDatabase(application)
    private val repository = Repository(database)

    private val viewModelJob= Job()
    private val coroutineScope = CoroutineScope(viewModelJob+ Dispatchers.IO)

    init {
        Log.i("DayEntryViewModel", "Created ViewModel")
    }

    fun updateDescription(description: String){
        this._description.value = description
    }

    fun setRating(rating: Int){
        _dayRating.value = rating
    }

    fun addURI(bitmapURI: String){
        if(!bitmapsURI.value.contains(bitmapURI))_bitmapsURI.value += bitmapURI
    }

    fun saveToDB(): Boolean{
        var result = false
        if(_description.value.trim()==""){
            _description.value = when(dayRating.value){
                1-> "To better days \uD83E\uDD42..."
                2->"To better days \uD83E\uDD42..."
                3->"Yet another day passed by..."
                4->"It was a good day..."
                5->"It was a great day..."
                else ->"Everything was alright \uD83D\uDE0C..."
            }
        }
        coroutineScope.launch {
            result = repository.saveToDatabase(bitmapsURI.value, description.value, dayRating.value)
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("DayEntryViewModel", "Cleared ViewModel")
    }
}
