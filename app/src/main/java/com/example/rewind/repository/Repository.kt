package com.example.rewind.repository

import com.example.rewind.domain.Day
import com.example.rewind.room.DayEntry
import com.example.rewind.room.RewindDatabase
import java.time.LocalDate

class Repository(private val database: RewindDatabase) {
    val dayData= mutableListOf(
        Day("Monday", 10293668567, "Good ig", 4),
        Day("Tuesday", 10293668567, "Good ig", 4),
        Day("Wednesday", 10293668567, "Good ig", 4),
        Day("Thursday", 10293668567, "Good ig", 4),
        Day("Friday", 10293668567, "Good ig", 4),
        Day("Saturday", 10293668567, "Good ig", 4),
        Day("Sunday", 10293668567, "Good ig", 4)
    )

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

//    suspend fun getData(): DayEntry {
//        return database.daysDao().getAll()
//    }

}