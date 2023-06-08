package com.xannanov.graduatework.domain.bt.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.xannanov.graduatework.domain.bt.model.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain =
    BluetoothDeviceDomain(
        name = name,
        mac = address
    )