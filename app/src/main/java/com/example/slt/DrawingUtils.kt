package com.example.slt

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

object DrawingUtils {

    private val landmarkPaint = Paint().apply {
        color = android.graphics.Color.GREEN
        strokeWidth = 8f
        style = Paint.Style.FILL
    }

    private val connectorPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        strokeWidth = 4f
    }

    // Define hand connections (pairs of landmark indices)
    val HAND_CONNECTIONS = listOf(
        Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 4),        // Thumb
        Pair(0, 5), Pair(5, 6), Pair(6, 7), Pair(7, 8),        // Index finger
        Pair(5, 9), Pair(9, 10), Pair(10, 11), Pair(11, 12),   // Middle finger
        Pair(9, 13), Pair(13, 14), Pair(14, 15), Pair(15, 16), // Ring finger
        Pair(13, 17), Pair(17, 18), Pair(18, 19), Pair(19, 20),// Pinky
        Pair(0, 17) // Palm base
    )

    fun drawLandmarks(
        canvas: Canvas,
        landmarks: List<NormalizedLandmark>
    ) {
        for (landmark in landmarks) {
            val point = PointF(landmark.x() * canvas.width, landmark.y() * canvas.height)
            canvas.drawCircle(point.x, point.y, 8f, landmarkPaint)
        }
    }

    fun drawConnectors(
        canvas: Canvas,
        landmarks: List<NormalizedLandmark>,
        connections: List<Pair<Int, Int>>
    ) {
        for (connection in connections) {
            val start = landmarks[connection.first]
            val end = landmarks[connection.second]
            val startPoint = PointF(start.x() * canvas.width, start.y() * canvas.height)
            val endPoint = PointF(end.x() * canvas.width, end.y() * canvas.height)

            canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, connectorPaint)
        }
    }
}
