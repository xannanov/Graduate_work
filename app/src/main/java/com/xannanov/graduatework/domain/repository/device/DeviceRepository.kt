package com.xannanov.graduatework.domain.repository.device

import com.xannanov.graduatework.domain.repository.device.model.DeviceModel

interface DeviceRepository {
    fun saveDevice(device: DeviceModel)
    fun getDeviceByUUID(uuid: String): DeviceModel?
    fun getAllDevices(): List<DeviceModel>
}