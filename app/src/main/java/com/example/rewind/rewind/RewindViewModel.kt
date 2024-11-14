package com.example.rewind.rewind

import android.app.Application
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.rewind.repository.Repository
import com.example.rewind.room.DayEntry
import com.example.rewind.room.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RewindViewModel(application: Application) : AndroidViewModel(application) {

    private val _description = mutableStateOf("")
    val description = derivedStateOf { this._description.value }

    private val _dayRating = MutableStateFlow(0)

    private val _bitmapsURI = MutableStateFlow<List<String>>(emptyList())
    val bitmapsURI = _bitmapsURI.asStateFlow()
//
    private val database = getDatabase(application)
    private val repository = Repository(database)

    private val viewModelJob= Job()
    private val coroutineScope = CoroutineScope(viewModelJob+ Dispatchers.IO)

    var dayEntry =MutableStateFlow<DayEntry?>(null)

    var dataURIs = repository.daysGoneBy

    fun setDayEntry(item: DayEntry?){
        dayEntry.value = item
    }

    fun addURI(bitmapURI: String){
        if(!bitmapsURI.value.contains(bitmapURI)) _bitmapsURI.value += bitmapURI
    }

    fun resetAll(){
        println("Clearing values")
        _bitmapsURI.value = emptyList()
        _description.value = ""
        _dayRating.value = 0
    }

    fun updateDayEntry(descriptionP: String, dayRating: Int, bitmapURIs: List<String>): Boolean {
        var description = descriptionP
        var result = false
        runBlocking {
            if (description.trim() == "") {
                description = when (dayRating) {
                    1 -> "To better days \uD83E\uDD42..."
                    2 -> "To better days \uD83E\uDD42..."
                    3 -> "Yet another day passed by..."
                    4 -> "It was a good day..."
                    5 -> "It was a great day..."
                    else -> "Everything was alright \uD83D\uDE0C..."
                }
            }
            Log.i("rewindViewModel", "bitmaps: $bitmapURIs")
            Log.i("rewindViewModel", "description: $description")
            Log.i("rewindViewModel", "dayRating: $dayRating")
            coroutineScope.launch {
                result = repository.saveToDatabase(bitmapURIs, description, dayRating)
            }
            Log.i("rewindViewModel", "saved: $result")
        }
        return result
    }

}