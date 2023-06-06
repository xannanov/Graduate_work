package com.xannanov.graduatework.di

import android.content.Context
import androidx.room.Room
import com.xannanov.graduatework.data.network.MyMQTTClient
import com.xannanov.graduatework.data.local.AppDatabase
import com.xannanov.graduatework.data.local.dao.DeviceDao
import com.xannanov.graduatework.domain.bt.BluetoothController
import com.xannanov.graduatework.domain.bt.BluetoothControllerImpl
import com.xannanov.graduatework.domain.repository.device.DeviceRepository
import com.xannanov.graduatework.domain.repository.device.DeviceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController =
        BluetoothControllerImpl(context)

    @Provides
    @Singleton
    fun provideDeviceRepository(deviceDao: DeviceDao): DeviceRepository =
        DeviceRepositoryImpl(deviceDao)

    @Provides
    @Singleton
    fun provideMqttClient(): MyMQTTClient = MyMQTTClient()

    @Provides
    @Singleton
    fun provideDeviceDao(appDatabase: AppDatabase): DeviceDao =
        appDatabase.deviceDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase =
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "AppDatabase"
        )
            .fallbackToDestructiveMigration()
            .build()
}