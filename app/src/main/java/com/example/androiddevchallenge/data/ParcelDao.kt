package com.example.androiddevchallenge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcel")
    fun getAll(): Flow<List<Parcel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(parcel: Parcel)
}
