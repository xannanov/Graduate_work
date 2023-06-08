package com.xannanov.graduatework.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.xannanov.graduatework.domain.repository.device.model.DeviceModel

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devicemodel")
    fun getAllDevices(): List<DeviceModel>

    @Query("SELECT * FROM devicemodel where uuid = :uuid limit 1")
    fun getDeviceByUUID(uuid: String): DeviceModel?

    @Insert
    fun saveDevice(deviceModel: DeviceModel)
}