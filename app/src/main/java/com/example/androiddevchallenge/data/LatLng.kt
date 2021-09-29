package com.example.androiddevchallenge.data

import kotlinx.serialization.Serializable

@Serializable
data class LatLng(
    val lat: Double,
    val lng: Double
)
