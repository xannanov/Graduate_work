package com.xannanov.graduatework.domain.bt

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.xannanov.graduatework.domain.bt.mapper.toBluetoothDeviceDomain
import com.xannanov.graduatework.domain.bt.mapper.toByteArray
import com.xannanov.graduatework.domain.bt.model.BTModel
import com.xannanov.graduatework.domain.bt.model.BluetoothDeviceDomain
import com.xannanov.graduatework.presentation.addnewdevice.bt.MessageFromIoTDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothControllerImpl(
    private val context: Context
) : BluetoothController {

    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    private val _scannedDevices = MutableStateFlow<List<BTModel>>(emptyList())
    override val scannedDevices: StateFlow<List<BTModel>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BTModel>>(emptyList())
    override val pairedDevices: StateFlow<List<BTModel>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val foundDeviceReceiver: FoundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver: BluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter.bondedDevices.contains(bluetoothDevice)) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can`t connect to non-paired device.")
            }
        }
    }

    private var currentServierSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No bluetooth connect permission")
            }

            currentServierSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                "key_exchange",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true

            while (shouldLoop) {
                currentClientSocket = try {
                    currentServierSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServierSocket?.close()
                    val service = BluetoothDataTransferService(it)

                    dataTransferService = service

                    emitAll(
                        service
                            .listenForIncomingMessages()
                            .map { message ->
                                ConnectionResult.TransferSucceeded(
                                    when {
                                        deviceConnectedRegexPattern.matches(message) ->
                                            MessageFromIoTDevice.MessageDeviceConnected(message)
                                        nextButtonRegexPattern.matches(message) ->
                                            MessageFromIoTDevice.MessageSettingNextButton(message)
                                        completeRegexPattern.matches(message) ->
                                            MessageFromIoTDevice.MessageSettingComplete(message)
                                        else ->
                                            throw IllegalArgumentException()
                                    }
                                )
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.mac)

            currentClientSocket = bluetoothDevice
                .createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))
            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)

                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it
                                .listenForIncomingMessages()
                                .map { message: String ->
                                    ConnectionResult.TransferSucceeded(
                                        when {
                                            deviceConnectedRegexPattern.matches(message) ->
                                                MessageFromIoTDevice.MessageDeviceConnected(message)
                                            nextButtonRegexPattern.matches(message) ->
                                                MessageFromIoTDevice.MessageSettingNextButton(message)
                                            completeRegexPattern.matches(message) ->
                                                MessageFromIoTDevice.MessageSettingComplete(message)
                                            else ->
                                                throw IllegalArgumentException()
                                        }
                                    )
                                }
                        )
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }

        if (dataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter.name ?: "Uncknown name",
            isFromLocalUser = true
        )

        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServierSocket?.close()
        currentServierSocket = null
        currentClientSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            .bondedDevices
            .map { it.toBluetoothDeviceDomain() }
            .also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val SERVICE_UUID = "00001101-0000-1000-8000-00805f9b34fb"
    }
}

val deviceConnectedRegexPattern =
    Regex("connected")
val nextButtonRegexPattern = Regex("next")
val completeRegexPattern = Regex("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}#.*\$")
