package com.example.rewind.entry

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
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

    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()

    val description = mutableStateOf("")

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

    fun addPhoto(bitmap: Bitmap){
        _bitmaps.value += bitmap
        Log.i("DayEntryViewModel", "bitmaps: ${bitmaps.value}")
    }

    fun updateDescription(description: String){
        this.description.value = description
    }

    fun setRating(rating: Int){
        _dayRating.value = rating
    }

    fun addURI(bitmapURI: String){
        _bitmapsURI.value += bitmapURI
    }

    fun saveToDB(){
        coroutineScope.launch {
//            runBlocking {
//                Log.i("DayEntryViewModel", repository.getData().toString())
//            }
            if(repository.saveToDatabase(bitmapsURI.value, description.value, dayRating.value)){
                //saved
            }
            else{

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("DayEntryViewModel", "Cleared ViewModel")
    }

//    var dayRating =

}