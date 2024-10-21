package com.example.rewind

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.rewind.domain.Day
import com.example.rewind.repository.Repository
import com.example.rewind.room.getDatabase

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val daysData = MutableLiveData<List<Day>>()

    private val database = getDatabase(application)
    private val repository = Repository(database)
    init{
        daysData.value = repository.dayData
    }
}