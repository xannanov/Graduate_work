package com.xannanov.graduatework.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xannanov.graduatework.data.local.dao.DeviceDao
import com.xannanov.graduatework.domain.repository.device.model.DeviceModel

@Database(entities = [DeviceModel::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
}