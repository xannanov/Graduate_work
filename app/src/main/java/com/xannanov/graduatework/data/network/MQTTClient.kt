package com.xannanov.graduatework.data.network

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kotlinx.coroutines.flow.*

class MyMQTTClient {

    private val _connectedState = MutableStateFlow(false)
    val connectedState: StateFlow<Boolean>
        get() = _connectedState.asStateFlow()
    private val _incomingMessages = MutableStateFlow("")
    val incomingMessages: StateFlow<String>
        get() = _incomingMessages.asStateFlow()

    private val host: String = "mqtt.by"
    private val username: String = "verzzil"
    private val password: String = "mueoq0gm"
    private val port: Int = 1883
    private val clientId: String = "andr"
    private val fullHostName = "tcp://$host:$port"
    private val topic: String = "/user/verzzil/iot"

    private var mqtt: Mqtt3AsyncClient? = MqttClient.builder()
        .useMqttVersion3()
        .identifier(clientId) // choose a unique client ID
        .serverHost(host) // replace with your broker's host name
        .serverPort(port) // replace with your broker's port number
        .automaticReconnectWithDefaultConfig()
        .buildAsync()

    fun connect() {
        mqtt?.connectWith()
            ?.simpleAuth()
            ?.username(username)
            ?.password(password.toByteArray())
            ?.applySimpleAuth()
            ?.send()
            ?.whenComplete { connAck, throwable ->
                if (throwable != null) {
                    Log.i("asdfasdf", "${throwable.message}")
                    throwable.printStackTrace()
                    // handle failure
                } else {
                    Log.i("asdfasdf", "connected")
                    _connectedState.update { true }
                    // setup subscribes or start publishing
                }
            }
    }

    fun publish(message: String) {
        mqtt?.publishWith()
            ?.topic(topic)
            ?.payload(message.toByteArray())
            ?.send()
            ?.whenComplete { publish, throwable ->
                if (throwable != null) {
//                    Log.i("asdfasdf", "publish failure ${throwable.message}")
                    // handle failure to publish
                } else {
//                    Log.i("asdfasdf", "publish success")
                    // handle successful publish, e.g. logging or incrementing a metric
                }
            }
    }

    fun subscribe() {
        mqtt?.subscribeWith()
            ?.topicFilter(topic)
            ?.qos(MqttQos.AT_LEAST_ONCE)
            ?.callback { mqtt5Publish ->
                _incomingMessages.update {
                    String(bytes = mqtt5Publish.payloadAsBytes, charset = Charsets.UTF_8)
                }
            }
            ?.send()
    }
}