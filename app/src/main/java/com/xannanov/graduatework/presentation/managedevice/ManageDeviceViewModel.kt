package com.xannanov.graduatework.presentation.managedevice

import androidx.lifecycle.ViewModel
import com.xannanov.graduatework.data.network.MyMQTTClient
import com.xannanov.graduatework.presentation.views.HandLandmarkerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageDeviceViewModel @Inject constructor(
    private val mqttClient: MyMQTTClient
): ViewModel() {

    init {
        mqttClient.connect()
    }

    fun publish(message: String) {
        mqttClient.publish(message)
    }

    private var _delegate: Int = HandLandmarkerHelper.DELEGATE_CPU
    private var _minHandDetectionConfidence: Float =
        HandLandmarkerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = HandLandmarkerHelper
        .DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = HandLandmarkerHelper
        .DEFAULT_HAND_PRESENCE_CONFIDENCE
    private var _maxHands: Int = HandLandmarkerHelper.DEFAULT_NUM_HANDS

    val currentDelegate: Int get() = _delegate
    val currentMinHandDetectionConfidence: Float
        get() =
            _minHandDetectionConfidence
    val currentMinHandTrackingConfidence: Float
        get() =
            _minHandTrackingConfidence
    val currentMinHandPresenceConfidence: Float
        get() =
            _minHandPresenceConfidence
    val currentMaxHands: Int get() = _maxHands

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setMinHandDetectionConfidence(confidence: Float) {
        _minHandDetectionConfidence = confidence
    }
    fun setMinHandTrackingConfidence(confidence: Float) {
        _minHandTrackingConfidence = confidence
    }
    fun setMinHandPresenceConfidence(confidence: Float) {
        _minHandPresenceConfidence = confidence
    }

    fun setMaxHands(maxResults: Int) {
        _maxHands = maxResults
    }
}