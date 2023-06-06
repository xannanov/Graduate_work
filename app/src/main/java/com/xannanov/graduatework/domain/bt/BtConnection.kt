package com.xannanov.graduatework.domain.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.stream.Collectors

class BtConnection(
    private val btAdapter: BluetoothAdapter
) {

    private val btCoroutineScope = CoroutineScope(Dispatchers.IO)

    private val uuid = UUID.randomUUID()
    private var socket: BluetoothSocket? = null

    fun connect(mac: String) {
        if (!btAdapter.isEnabled && mac.isEmpty()) return

        val device = btAdapter.getRemoteDevice(mac)

        device?.let {
            connect(it)
        }
    }

    private fun connect(device: BluetoothDevice) {
        btCoroutineScope.launch {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            try {
                Log.i("asdfasdf", "Connecting...")
                socket?.connect()
                Log.i("asdfasdf", "Connected. Start observing")
                startObserveSocket()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("asdfasdf", "Failed connection. ${e.message}")
            }
        }
    }

    private fun startObserveSocket() {
        val inStream = socket?.inputStream
        val outStream = socket?.outputStream

        btCoroutineScope.launch {
            val message = BufferedReader(InputStreamReader(inStream)).lines().collect(Collectors.joining("\n"))
            Log.i("asdfasdf", "$message")
        }
    }
}