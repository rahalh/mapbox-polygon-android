package com.example.androiddevchallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.androiddevchallenge.data.AppDatabase
import com.example.androiddevchallenge.data.Parcel
import com.example.androiddevchallenge.data.ParcelDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel()
class ParcelViewModel @Inject constructor(
    private val parcelDao: ParcelDao
) : ViewModel() {
    fun getParcels(): Flow<List<Parcel>> {
        return parcelDao.getAll()
    }

    fun createParcel(parcel: Parcel) {
        viewModelScope.launch {
            parcelDao.insert(parcel)
        }
    }
}
