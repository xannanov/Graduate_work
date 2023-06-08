package com.xannanov.graduatework.domain.repository.device

import com.xannanov.graduatework.data.local.dao.DeviceDao
import com.xannanov.graduatework.domain.repository.device.model.DeviceModel
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao
) : DeviceRepository {
    override fun saveDevice(device: DeviceModel) {
        deviceDao.saveDevice(device)
    }

    override fun getAllDevices(): List<DeviceModel> =
        deviceDao.getAllDevices()

    override fun getDeviceByUUID(uuid: String): DeviceModel? =
        deviceDao.getDeviceByUUID(uuid)
}