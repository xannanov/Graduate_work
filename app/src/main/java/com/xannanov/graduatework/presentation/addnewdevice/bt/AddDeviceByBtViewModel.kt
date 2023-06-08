package com.xannanov.graduatework.presentation.addnewdevice.bt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xannanov.graduatework.domain.bt.BluetoothController
import com.xannanov.graduatework.domain.bt.ConnectionResult
import com.xannanov.graduatework.domain.bt.model.BluetoothDeviceDomain
import com.xannanov.graduatework.domain.repository.device.DeviceRepository
import com.xannanov.graduatework.domain.repository.device.model.DeviceModel
import com.xannanov.graduatework.presentation.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BtState(
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
    val message: MessageFromIoTDevice? = null,
    val deviceSaved: Boolean = false,
    val deviceModel: DeviceModel? = null,
)

@HiltViewModel
class AddDeviceByBtViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    private val deviceRepository: DeviceRepository,
): ViewModel() {

    private val _state = MutableStateFlow(BtState())
    val state = combine(
        bluetoothController.scannedDevices,
        _state
    ) { scanned, state ->
        state.copy(
            scannedDevices = scanned.filter { it.name?.startsWith("smart") == true}
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private var deviceConnectionJob: Job? = null

    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
        bluetoothController.release()
    }

    fun saveDevice(uuid: String, type: DeviceType) {
        val name = "default name"
        val deviceModel = DeviceModel(uuid = uuid, name = name, type = type.toString())

        viewModelScope.launch(Dispatchers.IO) {
            if (deviceRepository.getDeviceByUUID(uuid) == null) {
                deviceRepository.saveDevice(deviceModel)
            }
            _state.update { it.copy(deviceSaved = true, deviceModel = deviceModel) }
        }
    }

    fun navigateToRootScreen() {
        Navigator.navigateToRoot()
    }

    fun navigateToSettingControl(uuid: String) {
        Navigator.navigateToManageDevice(uuid)
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update { it.copy(isConnecting = false, isConnected = false) }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            bluetoothController.trySendMessage(message)
        }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    private fun Flow<ConnectionResult>.listen(): Job = onEach { result ->
        when (result) {
            ConnectionResult.ConnectionEstablished ->
                _state.update {
                    it.copy(
                        isConnected = true,
                        isConnecting = false,
                        errorMessage = null
                    )
                }
            is ConnectionResult.Error ->
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                        errorMessage = result.message
                    )
                }
            is ConnectionResult.TransferSucceeded -> {
                _state.update {
                    it.copy(
                        message = result.message
                    )
                }
            }
        }
    }.catch { throwable ->
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnected = false,
                isConnecting = false,
            )
        }
    }.launchIn(viewModelScope)
}

sealed class MessageFromIoTDevice(open val message: String) {

    data class MessageDeviceConnected(
        override val message: String,
    ) : MessageFromIoTDevice(message)

    data class MessageSettingNextButton(
        override val message: String
    ) : MessageFromIoTDevice(message)

    data class MessageSettingComplete(
        override val message: String
    ) : MessageFromIoTDevice(message) {
        val deviceUUID: String
        val deviceType: DeviceType

        init {
            message.split("#").let {
                deviceUUID = it[0]
                deviceType = it[1].mapToDeviceType()
            }
        }
    }
}

enum class DeviceType {
    SMART_SOCKET, SMART_REMOTE_CONTROLLER
}

fun String.mapToDeviceType(): DeviceType =
    when (this) {
        "smart socket" -> DeviceType.SMART_SOCKET
        else -> DeviceType.SMART_REMOTE_CONTROLLER
    }