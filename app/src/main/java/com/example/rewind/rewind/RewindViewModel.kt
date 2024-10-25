package com.example.rewind.rewind

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rewind.repository.Repository
import com.example.rewind.room.DayEntry
import com.example.rewind.room.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RewindViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val repository = Repository(database)


    private val viewModelJob= Job()
    private val coroutineScope = CoroutineScope(viewModelJob+ Dispatchers.IO)

    private var dataURIs = emptyList<DayEntry>()

    init{
        coroutineScope.launch {
            dataURIs = repository.getData()
            Log.i("getData", dataURIs.toString())
        }
    }

    fun getData(): List<DayEntry>{
        return dataURIs
    }
}