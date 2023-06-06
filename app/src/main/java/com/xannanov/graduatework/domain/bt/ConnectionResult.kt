package com.xannanov.graduatework.domain.bt

import com.xannanov.graduatework.presentation.addnewdevice.bt.MessageFromIoTDevice

sealed interface ConnectionResult {
    object ConnectionEstablished: ConnectionResult
    data class TransferSucceeded(val message: MessageFromIoTDevice): ConnectionResult
    data class Error(val message: String): ConnectionResult
}