package com.example.slt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class DTWActivity : AppCompatActivity() {

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var videoView: VideoView
    private lateinit var tvPrediction: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: ImageView
    private val SELECT_VIDEO_REQUEST_CODE = 1001
    private var leftHandSequence: Array<FloatArray>? = null
    private var rightHandSequence: Array<FloatArray>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dtw)
        videoView = findViewById(R.id.videoView)
        tvPrediction = findViewById(R.id.tvPrediction)
        progressBar = findViewById(R.id.progressBar)
        closeButton = findViewById(R.id.closeBtn)

        // Initialize HandLandmarkerHelper in VIDEO mode
        handLandmarkerHelper = HandLandmarkerHelper(
            runningMode = com.google.mediapipe.tasks.vision.core.RunningMode.IMAGE,
            context = this,
            handLandmarkerHelperListener = null
        )

        findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            selectVideoFromGallery()
        }

        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun selectVideoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_VIDEO_REQUEST_CODE)
    }

    private fun loadTrainingData(context: Context): List<Triple<String, List<List<Float>>, List<List<Float>>>> {
        val labels = mutableListOf<Triple<String, List<List<Float>>, List<List<Float>>>>()
        val assetManager = context.assets
        val subDirs = assetManager.list("train_data") ?: return labels

        for (subDir in subDirs) {
            val jsonFiles = assetManager.list("train_data/$subDir") ?: continue
            for (jsonFile in jsonFiles) {
                if (jsonFile.endsWith(".json")) {
                    val inputStream = assetManager.open("train_data/$subDir/$jsonFile")
                    val jsonStr = inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonStr)

                    val label = jsonObject.getString("label")
                    val left = jsonObject.getJSONArray("left").to2DList()
                    val right = jsonObject.getJSONArray("right").to2DList()

                    labels.add(Triple(label, left, right))
                }
            }
        }
        return labels
    }

    fun JSONArray.to2DList(): List<List<Float>> {
        val outerList = mutableListOf<List<Float>>()
        for (i in 0 until length()){
            val innerArray = getJSONArray(i)
            val innerList = mutableListOf<Float>()
            for (j in 0 until innerArray.length()) {
                val point = innerArray.getJSONArray(j)
                for (k in 0 until point.length()) {
                    innerList.add(point.getDouble(k).toFloat())
                }
            }
            outerList.add(innerList)
        }
        return outerList
    }

    fun dtwDistance(seqA: List<List<Float>>, seqB: List<List<Float>>): Float {
        val n = seqA.size
        val m = seqB.size
        val dtw = Array(n + 1) { FloatArray(m + 1) { Float.POSITIVE_INFINITY } }
        dtw[0][0] = 0f

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = l2(seqA[i - 1], seqB[j - 1])
                dtw[i][j] = cost + minOf(dtw[i - 1][j], dtw[i][j - 1], dtw[i - 1][j - 1])
            }
        }

        return dtw[n][m]
    }

    fun l2(vec1: List<Float>, vec2: List<Float>): Float {
        // Calculate sum of squared differences manually using fold
        val sumOfSquares = vec1.zip(vec2).fold(0f) { acc, (a, b) ->
            acc + ((a - b).toDouble().pow(2)).toFloat()
        }
        // Return the square root of the sum
        return sqrt(sumOfSquares.toDouble()).toFloat()
    }

    fun predictSign(
        testLeft: List<List<Float>>,
        testRight: List<List<Float>>,
        trainData: List<Triple<String, List<List<Float>>, List<List<Float>>>>
    ): String {
        var bestLabel = ""
        var bestScore = Float.POSITIVE_INFINITY

        for ((label, trainLeft, trainRight) in trainData) {
            val leftDist = dtwDistance(testLeft, trainLeft)
            val rightDist = dtwDistance(testRight, trainRight)
            val totalDist = leftDist + rightDist

            if (totalDist < bestScore) {
                bestScore = totalDist
                bestLabel = label
            }
        }
        return bestLabel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { videoUri ->
                processVideoForLandmarks(videoUri)
            }
        }
    }


    private fun processVideoForLandmarks(videoUri: Uri) {
        progressBar.visibility = View.VISIBLE
        tvPrediction.text = "Processing video..."

        Thread {
            val extractor = MediaMetadataRetriever()
            extractor.setDataSource(this, videoUri)

            val durationStr = extractor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val frameIntervalMs = 100L

            val leftSequence = mutableListOf<List<Float>>()
            val rightSequence = mutableListOf<List<Float>>()

            var timeMs = 0L
            val annotatedBitmaps = mutableListOf<Bitmap>()
            while (timeMs < durationMs) {
                val bitmap = extractor.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                bitmap?.let {
                    val (left, right, annotatedBitmap) = handLandmarkerHelper.detectHandsWithBitmap(it)
                    leftSequence.add(left)
                    rightSequence.add(right)
                    annotatedBitmaps.add(annotatedBitmap)

                }
                timeMs += frameIntervalMs
            }
            extractor.release()

            val maxFrames = 168 // Set your preferred frame count
            val leftHandSequenceTemp = matchSequenceLength(leftSequence.map { it.toFloatArray() }.toTypedArray(), maxFrames)
            val rightHandSequenceTemp = matchSequenceLength(rightSequence.map { it.toFloatArray() }.toTypedArray(), maxFrames)
            leftHandSequence = leftHandSequenceTemp
            rightHandSequence = rightHandSequenceTemp

            runOnUiThread{
                progressBar.visibility = View.GONE
                tvPrediction.text = "Landmark extraction complete: ${leftSequence.size} frames."

                val mediaController = android.widget.MediaController(this)
                mediaController.setAnchorView(videoView)
                videoView.setMediaController(mediaController)

                videoView.visibility = View.VISIBLE
                videoView.setVideoURI(videoUri)
                videoView.start()
                runDTWModel()
            }
        }.start()
    }


    private fun runDTWModel() {
        if (leftHandSequence == null || rightHandSequence == null) {
            tvPrediction.text = "No landmarks to process"
            return
        }

        val combinedSequence = leftHandSequence!!.zip(rightHandSequence!!) { left, right -> left + right }.toTypedArray()
        val flattened = combinedSequence.flatMap { it.toList() }.toFloatArray()

        // Load training data
        val trainData = loadTrainingData(applicationContext)

        // Predict using DTW distance comparison
        val predictedSign = predictSign(
            leftHandSequence!!.map { it.toList() },
            rightHandSequence!!.map { it.toList() },
            trainData
        )

        tvPrediction.text = "Predicted Sign: $predictedSign"
    }

    private fun matchSequenceLength(sequence: Array<FloatArray>, maxFrames: Int): Array<FloatArray> {
        return if (sequence.size > maxFrames) {
            sequence.copyOfRange(0, maxFrames)  // Truncate the sequence if too long
        } else {
            // Pad with zeros if the sequence is too short
            val padding = Array(maxFrames - sequence.size) { FloatArray(sequence[0].size) }
            sequence + padding
        }
    }
}
