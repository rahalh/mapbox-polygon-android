package com.example.androiddevchallenge.data

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ProvidedTypeConverter
class ParcelTypeConverters {
    @TypeConverter
    fun latLngArrayToString(points: List<LatLng>): String = Json.encodeToString(points)

    @TypeConverter
    fun stringToLatLngArray(points: String): List<LatLng> = Json.decodeFromString(points)
}
