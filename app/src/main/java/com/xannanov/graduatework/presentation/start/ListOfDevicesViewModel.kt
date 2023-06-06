package com.xannanov.graduatework.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xannanov.graduatework.domain.repository.device.DeviceRepository
import com.xannanov.graduatework.domain.repository.device.model.DeviceModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceListState(
    val devices: List<DeviceModel> = emptyList()
)

@HiltViewModel
class ListOfDevicesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val state: StateFlow<DeviceListState>
        get() = _state.asStateFlow()
    private val _state = MutableStateFlow(DeviceListState())

    fun getAllDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            val devices = deviceRepository.getAllDevices()
            _state.update {
                it.copy(devices = devices)
            }
        }
    }
}