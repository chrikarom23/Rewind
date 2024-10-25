package com.example.rewind.repository

import com.example.rewind.domain.Day
import com.example.rewind.room.DayEntry
import com.example.rewind.room.RewindDatabase
import java.time.LocalDate

class Repository(private val database: RewindDatabase) {


    suspend fun saveToDatabase(bitmapsURI: List<String>, description: String, dayRating: Int): Boolean {
        val localDate = LocalDate.now()

        return try{
            database.daysDao().insertDay(DayEntry(localDate.dayOfWeek.toString(),
                localDate.toString(),description,dayRating,bitmapsURI))
            true
        }
        catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    suspend fun getData(): List<DayEntry> {
        return database.daysDao().getAll()
    }

}