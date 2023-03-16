package com.xannanov.graduatework

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity() {

    private lateinit var btn: Button
    private lateinit var disconnect: Button
    private lateinit var publish: Button
    private lateinit var subscribe: Button
    private lateinit var tvData: EditText

    private val scope = CoroutineScope(Dispatchers.Main)

    val host = "1c82df1089854a619419b7f2f1c4f276.s2.eu.hivemq.cloud"
    val username = "test2"
    val password = "qwerty007"
    private lateinit var client: Mqtt5BlockingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn = findViewById(R.id.btn)
        disconnect = findViewById(R.id.disconnect)
        publish = findViewById(R.id.btn_publish)
        subscribe = findViewById(R.id.btn_subscribe)
        tvData = findViewById(R.id.tv_data)

        client = MqttClient.builder()
            .useMqttVersion5()
            .serverHost(host)
            .serverPort(8883)
            .sslWithDefaultConfig()
            .buildBlocking()

        btn.setOnClickListener {
            client.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.toByteArray(Charsets.UTF_8))
                .applySimpleAuth()
                .send()

            Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show()

            client.subscribeWith()
                .topicFilter("my/test/topic")
                .send()
        }

        publish.setOnClickListener {
            client.publishWith()
                .topic("my/test/topic")
                .payload(tvData.text.toString().toByteArray(Charsets.UTF_8))
                .send()
        }

        subscribe.setOnClickListener {
            client.subscribeWith()
                .topicFilter("my/test/topic")
                .send();

            // set a callback that is called when a message is received (using the async API style)
            client.toAsync().publishes(MqttGlobalPublishFilter.ALL) { publish ->
                scope.launch {
                    Toast.makeText(
                        this@MainActivity, "Received message: " +
                                publish.topic + " -> " +
                                UTF_8.decode(publish.payload.get()), Toast.LENGTH_SHORT
                    ).show()
                }

                // disconnect the client after a message was received
            }
        }

        disconnect.setOnClickListener {
            client.disconnect()
            Toast.makeText(this, "disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        client.disconnect()
        super.onDestroy()
    }

    companion object {
        const val SERVER_URI = "tcp://1c82df1089854a619419b7f2f1c4f276.s2.eu.hivemq.cloud:8883"
    }
}