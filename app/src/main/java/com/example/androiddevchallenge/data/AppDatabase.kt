package com.example.androiddevchallenge.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Parcel::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ParcelTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
}
