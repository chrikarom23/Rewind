package com.example.rewind.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Dao
interface DaysDao{
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDay(dayEntry: DayEntry)

    @Query("SELECT * FROM DAY_ENTRY LIMIT 1")
    suspend fun getAll(): DayEntry
}

@Database(entities = [DayEntry::class], version=1, exportSchema = true)
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
            ).fallbackToDestructiveMigrationFrom(1).build()
        }
    }
    return INSTANCE
}