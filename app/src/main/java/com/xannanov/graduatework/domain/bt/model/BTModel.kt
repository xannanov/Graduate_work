package com.xannanov.graduatework.domain.bt.model

typealias BluetoothDeviceDomain = BTModel

data class BTModel(
    val name: String?,
    val mac: String
): java.io.Serializable