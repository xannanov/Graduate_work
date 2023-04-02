package com.xannanov.graduatework

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hivemq.client.mqtt.MqttClient
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var btn: Button
    private lateinit var disconnect: Button
    private lateinit var publish: Button
    private lateinit var subscribe: Button
    private lateinit var tvData: EditText

    private val scope = CoroutineScope(Dispatchers.Main)

    val host = "mqtt.by"
    //val username = "albaverz"
    val username = "verzzil"
    val password = "mueoq0gm"
    //val password = "rl4hscnq"
    val port = 1883
    val clientId = "andr"
    val fullHostName = "tcp://$host:$port"
    val topic = "/user/verzzil/iot"

    var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val mqtt = MqttClient.builder()
            .useMqttVersion3()
            .identifier(clientId) // choose a unique client ID
            .serverHost(host) // replace with your broker's host name
            .serverPort(port) // replace with your broker's port number
            .automaticReconnectWithDefaultConfig()
            .buildAsync()

        btn = findViewById(R.id.btn)
        disconnect = findViewById(R.id.disconnect)
        publish = findViewById(R.id.btn_publish)
        subscribe = findViewById(R.id.btn_subscribe)
        tvData = findViewById(R.id.tv_data)

        btn.setOnClickListener {
            mqtt.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.toByteArray())
                .applySimpleAuth()
                .send()
                .whenComplete { connAck, throwable ->
                    if (throwable != null) {
                        Log.i("asdfasdf", "${throwable.message}")
                        Toast.makeText(this, "failure ${throwable.message}", Toast.LENGTH_SHORT).show()
                        throwable.printStackTrace()
                        // handle failure
                    } else {
                        Log.i("asdfasdf", "connected")
                        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show()
                        // setup subscribes or start publishing
                    }
                }
        }

        publish.setOnClickListener {
            flag = !flag
            mqtt.publishWith()
                .topic(topic)
                .payload(if (flag) "on".toByteArray() else "off".toByteArray())
                .send()
                .whenComplete { publish, throwable ->
                    if (throwable != null) {
                        Log.i("asdfasdf", "publish failure ${throwable.message}")
                        // handle failure to publish
                    } else {
                        Log.i("asdfasdf", "publish success")
                        // handle successful publish, e.g. logging or incrementing a metric
                    }
                }
        }

        subscribe.setOnClickListener {
        }

        disconnect.setOnClickListener {
        }
    }

    override fun onDestroy() {
        Log.i("asdfasdf", "destroyed")
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val SERVER_URI = "tcp://1c82df1089854a619419b7f2f1c4f276.s2.eu.hivemq.cloud:8883"
    }
}

fun main() {

    /*runBlocking {
        val list = listOf<String>()
        list.indexOf()

        println("start runBlocking")
        launch {
            delay(2000)
            println("launch ready")
        }
        Thread {
            Thread.sleep(2000)
            println("thread ready")
        }.start()
        println("stop runBlocking")
    }*/
}