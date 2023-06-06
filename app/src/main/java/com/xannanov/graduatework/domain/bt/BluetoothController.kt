package com.xannanov.graduatework.domain.bt

import com.xannanov.graduatework.domain.bt.model.BTModel
import com.xannanov.graduatework.domain.bt.model.BluetoothDeviceDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BTModel>>
    val pairedDevices: StateFlow<List<BTModel>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult>
    fun closeConnection()

    suspend fun trySendMessage(message: String): BluetoothMessage?

    fun release()
}