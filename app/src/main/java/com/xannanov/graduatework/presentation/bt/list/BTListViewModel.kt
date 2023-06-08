package com.xannanov.graduatework.presentation.bt.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xannanov.graduatework.domain.bt.BluetoothController
import com.xannanov.graduatework.domain.bt.BluetoothMessage
import com.xannanov.graduatework.domain.bt.ConnectionResult
import com.xannanov.graduatework.domain.bt.model.BluetoothDeviceDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val pairedDevices: List<BluetoothDeviceDomain> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = emptyList(),
)

@HiltViewModel
class BTListViewModel @Inject constructor(
    private val btController: BluetoothController
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        btController.scannedDevices,
        btController.pairedDevices,
        _state
    ) { scanned, paired, state ->
        state.copy(
            scannedDevices = scanned,
            pairedDevices = paired,
            messages = if (state.isConnected) state.messages else emptyList()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private var deviceConnectionJob: Job? = null

    init {
        btController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        btController.errors.onEach { error ->
            _state.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = btController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        btController.closeConnection()
        _state.update { it.copy(isConnecting = false, isConnected = false) }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val bluetoothMessage = btController.trySendMessage(message)
        }
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = btController
            .startBluetoothServer()
            .listen()
    }

    fun startScan() {
        btController.startDiscovery()
    }

    fun stopScan() {
        btController.stopDiscovery()
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
//                        messages = it.messages + result.message
                    )
                }
            }
        }
    }.catch { throwable ->
        btController.closeConnection()
        _state.update {
            it.copy(
                isConnected = false,
                isConnecting = false,
            )
        }
    }.launchIn(viewModelScope)

    override fun onCleared() {
        super.onCleared()
        btController.release()
    }
}