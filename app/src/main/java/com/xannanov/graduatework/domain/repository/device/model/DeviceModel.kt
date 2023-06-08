package com.xannanov.graduatework.domain.repository.device.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeviceModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String,
    val name: String? = null,
    val room: String? = null,
    val type: String? = null
)