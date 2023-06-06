package com.xannanov.graduatework.presentation.addnewdevice.bt.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xannanov.graduatework.data.network.MyMQTTClient
import com.xannanov.graduatework.domain.bt.BluetoothController
import com.xannanov.graduatework.domain.repository.device.DeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*

data class SettingSmartControlState(
    val isReady: Boolean = false,
    val incomingMessage: String? = null
)

class SettingSmartControlViewModel @AssistedInject constructor(
    private val bluetoothController: BluetoothController,
    private val deviceRepository: DeviceRepository,
    private val mqttClient: MyMQTTClient,
    @Assisted
    private val uuid: String,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingSmartControlState())
    val state = combine(
        mqttClient.incomingMessages,
        _state
    ) { mqttIncomingMessages, state ->
        state.copy(
            incomingMessage = mqttIncomingMessages,
            isReady = mqttIncomingMessages.split("#")[0] == uuid &&
                    mqttIncomingMessages.split("#")[1] == "READY"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    init {
        mqttClient.connect()
    }


    @AssistedFactory
    interface Factory {
        fun create(uuid: String): SettingSmartControlViewModel
    }

    companion object {
        fun provideSettingSmartControlViewModelFactory(factory: Factory, uuid: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(uuid) as T
                }
            }
    }
}