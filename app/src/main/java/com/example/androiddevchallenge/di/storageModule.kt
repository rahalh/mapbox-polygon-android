package com.example.androiddevchallenge.di

import android.app.Application
import androidx.room.Room
import com.example.androiddevchallenge.data.AppDatabase
import com.example.androiddevchallenge.data.ParcelDao
import com.example.androiddevchallenge.data.ParcelTypeConverters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

    @Provides
    @Singleton
    fun provideParcelTypeConverter(): ParcelTypeConverters {
        return ParcelTypeConverters()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application, parcelTypeConverters: ParcelTypeConverters): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "android-challenge"
        ).addTypeConverter(parcelTypeConverters).build()
    }

    @Provides
    @Singleton
    fun provideParcelDao(db: AppDatabase): ParcelDao {
        return db.parcelDao()
    }
}
