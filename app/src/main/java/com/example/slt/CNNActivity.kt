package com.example.slt

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CNNActivity : AppCompatActivity() {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var poseLandmarker: PoseLandmarker
    lateinit var imageView: ImageView
    private lateinit var interpreter: Interpreter
    lateinit var tvPrediction: android.widget.TextView
    private lateinit var closeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cnn)

        imageView = findViewById(R.id.imageView)
        closeButton = findViewById(R.id.closeBtn)
        tvPrediction = findViewById(R.id.tvPrediction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializePoseLandmarker()

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Flip the image horizontally
                val flippedBitmap = flipImageHorizontally(bitmap)

                val (features, detected, annotatedBitmap) = extractHandFeaturesFromImage(flippedBitmap)

                if (detected) {
                    Log.i("HandFeature", "Features extracted: ${features.joinToString()}")
                    imageView.setImageBitmap(annotatedBitmap)

                    val prediction = classifySign(features)
                    tvPrediction.text = "Predicted Sign: $prediction"
                    Log.i("Prediction", "Sign: $prediction")
                } else {
                    Log.e("HandFeature", "No hand detected.")
                }

            }
        }

        val btnSelectImage: Button = findViewById(R.id.btnSelectImage)
        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        closeButton.setOnClickListener{
            finish()
        }

        loadInterpreter()

        val inputShape = interpreter.getInputTensor(0).shape()
        Log.i("TFLite", "Model input shape: ${inputShape.joinToString()}")
    }

    private fun initializePoseLandmarker() {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_full.task")  // Make sure this file is in assets folder
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE) // Since we're using static images
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(this, options)
    }

    private fun flipImageHorizontally(inputBitmap: Bitmap): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postScale(-1f, 1f, inputBitmap.width / 2f, inputBitmap.height / 2f)

        return Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
    }

    private fun runPoseDetectionOnBitmap(bitmap: Bitmap) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = poseLandmarker.detect(mpImage)

        if (result.landmarks().isEmpty()) {
            Log.e("GalleryPoseLandmarks", "No pose landmarks detected.")
            return
        }

        var count = 0
        result.landmarks().forEach { landmarkList ->
            landmarkList.forEach {
                Log.d("GalleryPoseLandmarks", "landmark: $count , x = ${it.x()}, y = ${it.y()}, z = ${it.z()}")
                count++
            }
        }

        Log.i("GalleryPoseLandmarks", "Total landmarks detected: $count")
    }

    private fun extractHandFeaturesFromImage(inputBitmap: Bitmap): Triple<FloatArray, Boolean, Bitmap?> {
        val landmarkData = FloatArray(63) // 21 landmarks * (x, y, z)
        var handDetected = false
        var annotatedBitmap: Bitmap? = null

        // Create hand landmarker
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task") // Must be placed in assets folder
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumHands(1)
            .build()

        val handLandmarker = HandLandmarker.createFromOptions(this, options)

        val mpImage = BitmapImageBuilder(inputBitmap).build()
        val result = handLandmarker.detect(mpImage)

        val imageWidth = inputBitmap.width.toFloat()
        val imageHeight = inputBitmap.height.toFloat()

        // Create a copy of the bitmap to annotate
        annotatedBitmap = inputBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = android.graphics.Canvas(annotatedBitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GREEN
            style = android.graphics.Paint.Style.FILL
            strokeWidth = 6f
        }

        // Extract landmarks if available
        if (result.handedness().isNotEmpty() && result.landmarks().isNotEmpty()) {
            handDetected = true
            val handLandmarks = result.landmarks()[0]

            for ((i, landmark) in handLandmarks.withIndex()) {
                val x = landmark.x() * imageWidth
                val y = landmark.y() * imageHeight
                val z = landmark.z() * imageWidth // Already in normalized 3D space

                landmarkData[i * 3] = x
                landmarkData[i * 3 + 1] = y
                landmarkData[i * 3 + 2] = z

                // Draw circle on each landmark for annotation
                canvas.drawCircle(x, y, 8f, paint)
            }
        } else {
            // If no hand is detected, landmarkData remains zeros
            Log.w("HandDetection", "No hand detected in image.")
        }

        handLandmarker.close()

        return Triple(landmarkData, handDetected, annotatedBitmap)
    }

    private fun classifySign(landmarks: FloatArray): String {
        val inputBuffer = ByteBuffer.allocateDirect(63 * 4).apply {
            order(ByteOrder.nativeOrder())
            for (value in landmarks) {
                putFloat(value)
            }
            rewind()
        }

        val output = Array(1) { FloatArray(36) } // A-Z

        interpreter.run(inputBuffer, output)

        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val labelList = listOf(
            "1", "10", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        )

        return if (predictedIndex in labelList.indices) labelList[predictedIndex] else "Unknown"
    }

    private fun loadInterpreter() {
        val modelBytes = assets.open("CNNModel.tflite").readBytes()
        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size).apply {
            order(ByteOrder.nativeOrder())
            put(modelBytes)
            rewind()
        }

        interpreter = Interpreter(modelBuffer)
    }
}
