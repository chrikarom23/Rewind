package com.example.rewind.room

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Upsert


@Dao
interface DaysDao{
    @Upsert
    suspend fun insertDay(dayEntry: DayEntry)

    @Query("SELECT * FROM DAY_ENTRY ORDER BY date DESC")
    fun getAll(): LiveData<List<DayEntry>>

    @Query("SELECT * FROM DAY_ENTRY ORDER BY RANDOM() LIMIT 1")
    fun getRandomDay(): DayEntry
}

@Database(entities = [DayEntry::class], version=2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class RewindDatabase: RoomDatabase(){
    abstract fun daysDao(): DaysDao
}

private lateinit var INSTANCE:RewindDatabase

fun getDatabase(context: Context): RewindDatabase{
    synchronized(RewindDatabase::class.java){
        if(!::INSTANCE.isInitialized){
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                RewindDatabase::class.java,
                "newsDatabase"
            ).fallbackToDestructiveMigrationFrom(1,2).build()
        }
    }
    return INSTANCE
}