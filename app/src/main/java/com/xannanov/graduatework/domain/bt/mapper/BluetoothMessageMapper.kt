package com.xannanov.graduatework.domain.bt.mapper

import com.xannanov.graduatework.domain.bt.BluetoothMessage

fun BluetoothMessage.toByteArray(): ByteArray =
    message.encodeToByteArray()

fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage =
    BluetoothMessage(
        senderName = substringBeforeLast("#"),
        message = substringAfter("#"),
        isFromLocalUser = isFromLocalUser
    )