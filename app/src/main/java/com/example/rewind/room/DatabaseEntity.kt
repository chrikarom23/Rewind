package com.example.rewind.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Entity(tableName = "day_entry")
data class DayEntry (
    @ColumnInfo(name = "day_word")
    val dayWord: String,
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "description")
    var description: String,
    @ColumnInfo(name = "day_rating")
    val dayRating: Int,
    @ColumnInfo(name = "imageURI")
    val imageURIS: List<String>
){
    override fun toString(): String {
        return buildString {
            append(dayWord)
            append("\n")
            append(date)
            append("\n")
            append(description)
            append("\n")
            append(dayRating)
            append("\n")
            append(imageURIS)
            append("\n")
        }
    }
}


class Converters {

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }
}