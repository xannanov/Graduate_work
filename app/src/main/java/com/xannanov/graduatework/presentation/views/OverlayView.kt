/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xannanov.graduatework.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.xannanov.graduatework.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _detectFlow: MutableStateFlow<Gesture> = MutableStateFlow(Gesture.IDLE)

    val detectFlow = _detectFlow.asStateFlow()

    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var largePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 50f
        style = Paint.Style.FILL_AND_STROKE
    }

    private val littleFinger = Finger.LittleFinger(null, null)
    private val ringFinger = Finger.RingFinger(null, null)
    private val middleFinger = Finger.MiddleFinger(null, null)
    private val forefinger = Finger.Forefinger(null, null)
    private val thumb = Finger.Thumb(null, null)

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.purple_200)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { handLandmarkerResult ->
            val lines = mutableListOf<Float>()
            val points = mutableListOf<Float>()
            val largePoints = mutableListOf<Float>()

            for (landmarks in handLandmarkerResult.landmarks()) {
                for (i in landmarkConnections.indices step 2) {
                    val startX =
                        landmarks[landmarkConnections[i]].x() * imageWidth * scaleFactor
                    val startY =
                        landmarks[landmarkConnections[i]].y() * imageHeight * scaleFactor
                    val endX =
                        landmarks[landmarkConnections[i + 1]].x() * imageWidth * scaleFactor
                    val endY =
                        landmarks[landmarkConnections[i + 1]].y() * imageHeight * scaleFactor
                    lines.add(startX)
                    lines.add(startY)
                    lines.add(endX)
                    lines.add(endY)

                    defineFingersTip(i, endX, endY, largePoints)
                    points.add(startX)
                    points.add(startY)
                }
                canvas.drawLines(lines.toFloatArray(), linePaint)
                canvas.drawPoints(points.toFloatArray(), pointPaint)
                canvas.drawPoints(largePoints.toFloatArray(), largePaint)
            }

            if (forefinger.x != null && forefinger.y != null && middleFinger.x != null && middleFinger.y != null)
                if (getDistance(forefinger.x!!, forefinger.y!!, middleFinger.x!!, middleFinger.y!!) < 150) {
                    coroutineScope.launch {
                        _detectFlow.emit(Gesture.On)
                    }
                } else {
                    coroutineScope.launch {
                        _detectFlow.emit(Gesture.Off)
                    }
                }
        }
    }

    override fun onDetachedFromWindow() {
        coroutineScope.cancel()
        super.onDetachedFromWindow()
    }

    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Double =
        sqrt((x1 - x2).toDouble().pow(2) + (y1 - y2).toDouble().pow(2))

    private fun defineFingersTip(
        i: Int,
        endX: Float,
        endY: Float,
        largePoints: MutableList<Float>
    ) {
        when (i) {
            // мезинец
            LITTLE_FINGER_TIP -> {
                littleFinger.x = endX
                littleFinger.y = endY
                largePoints.add(littleFinger.x!!)
                largePoints.add(littleFinger.y!!)
            }
            //безымянный
            RING_FINGER_TIP -> {
                ringFinger.x = endX
                ringFinger.y = endY
                largePoints.add(ringFinger.x!!)
                largePoints.add(ringFinger.y!!)
            }
            // указательный
            FOREFINGER_TIP -> {
                forefinger.x = endX
                forefinger.y = endY
                largePoints.add(forefinger.x!!)
                largePoints.add(forefinger.y!!)
            }
            // большой палец
            THUMB_TIP -> {
                thumb.x = endX
                thumb.y = endY
                largePoints.add(thumb.x!!)
                largePoints.add(thumb.y!!)
            }
            // средний палец
            MIDDLE_TIP -> {
                middleFinger.x = endX
                middleFinger.y = endY
                largePoints.add(middleFinger.x!!)
                largePoints.add(middleFinger.y!!)
            }
        }
    }

    fun setResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = handLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F

        private const val LITTLE_FINGER_TIP = 30
        private const val RING_FINGER_TIP = 40
        private const val FOREFINGER_TIP = 14
        private const val THUMB_TIP = 6
        private const val MIDDLE_TIP = 22

        // This list defines the lines that are drawn when visualizing the hand landmark detection
        // results. These lines connect:
        // landmarkConnections[2*n] and landmarkConnections[2*n+1]
        private val landmarkConnections = listOf(
            HandLandmark.WRIST,
            HandLandmark.THUMB_CMC,
            HandLandmark.THUMB_CMC,
            HandLandmark.THUMB_MCP,
            HandLandmark.THUMB_MCP,
            HandLandmark.THUMB_IP,
            HandLandmark.THUMB_IP,
            HandLandmark.THUMB_TIP,
            HandLandmark.WRIST,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.INDEX_FINGER_PIP,
            HandLandmark.INDEX_FINGER_PIP,
            HandLandmark.INDEX_FINGER_DIP,
            HandLandmark.INDEX_FINGER_DIP,
            HandLandmark.INDEX_FINGER_TIP,
            HandLandmark.INDEX_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.MIDDLE_FINGER_PIP,
            HandLandmark.MIDDLE_FINGER_PIP,
            HandLandmark.MIDDLE_FINGER_DIP,
            HandLandmark.MIDDLE_FINGER_DIP,
            HandLandmark.MIDDLE_FINGER_TIP,
            HandLandmark.MIDDLE_FINGER_MCP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.RING_FINGER_PIP,
            HandLandmark.RING_FINGER_PIP,
            HandLandmark.RING_FINGER_DIP,
            HandLandmark.RING_FINGER_DIP,
            HandLandmark.RING_FINGER_TIP,
            HandLandmark.RING_FINGER_MCP,
            HandLandmark.PINKY_MCP,
            HandLandmark.WRIST,
            HandLandmark.PINKY_MCP,
            HandLandmark.PINKY_MCP,
            HandLandmark.PINKY_PIP,
            HandLandmark.PINKY_PIP,
            HandLandmark.PINKY_DIP,
            HandLandmark.PINKY_DIP,
            HandLandmark.PINKY_TIP
        )
    }
}

sealed class Finger(var x: Float?, var y: Float?) {
    class LittleFinger(x: Float?, y: Float?) : Finger(x, y)
    class RingFinger(x: Float?, y: Float?) : Finger(x, y)
    class MiddleFinger(x: Float?, y: Float?) : Finger(x, y)
    class Forefinger(x: Float?, y: Float?) : Finger(x, y)
    class Thumb(x: Float?, y: Float?) : Finger(x, y)
}

sealed class Gesture {

    object IDLE : Gesture()
    object Off : Gesture()
    object On : Gesture()
}
