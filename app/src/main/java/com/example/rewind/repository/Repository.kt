package com.example.rewind.repository

import androidx.lifecycle.LiveData
import com.example.rewind.room.DayEntry
import com.example.rewind.room.RewindDatabase
import java.time.LocalDate

class Repository(private val database: RewindDatabase) {

    suspend fun saveToDatabase(bitmapsURI: List<String>, description: String, dayRating: Int): Boolean {
        val localDate = LocalDate.now()
        println(localDate.toString())
        return try{
            database.daysDao().insertDay(DayEntry(pascalCase(localDate.dayOfWeek.toString()),
                localDate.toString(),description,dayRating,bitmapsURI))
            true
        }
        catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
    
    private fun pascalCase(dayOfWeek: String): String {
        return buildString {
            for(i in dayOfWeek.indices){
                if(i==0) append(dayOfWeek[i])
                else append(dayOfWeek[i].lowercaseChar())
            }
        }
    }

    val daysGoneBy: LiveData<List<DayEntry>> = database.daysDao().getAll()
}