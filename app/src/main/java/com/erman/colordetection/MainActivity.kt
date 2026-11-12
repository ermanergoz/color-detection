package com.erman.colordetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private lateinit var cameraView: CameraBridgeViewBase
    private lateinit var touchCoordinates: TextView
    private lateinit var touchColor: TextView

    @Volatile
    private var frame: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        touchCoordinates = findViewById(R.id.touch_coordinates)
        touchColor = findViewById(R.id.touch_color)

        cameraView = findViewById(R.id.opencv_tutorial_activity_surface_view)
        cameraView.visibility = View.VISIBLE
        cameraView.setCvCameraViewListener(this)
        cameraView.setOnTouchListener(this)

        if (hasCameraPermission()) {
            cameraView.setCameraPermissionGranted()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && hasCameraPermission()) {
            cameraView.setCameraPermissionGranted()
        }
    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            cameraView.enableView()
        } else {
            Log.e(TAG, "OpenCV could not be loaded")
        }
    }

    override fun onPause() {
        super.onPause()
        cameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {
        frame = null
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val rgba = inputFrame.rgba()
        frame = rgba
        return rgba
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val rgba = frame ?: return false
        val cols = rgba.cols()
        val rows = rgba.rows()
        if (cols == 0 || rows == 0) return false

        val scale = minOf(view.width.toFloat() / cols, view.height.toFloat() / rows)
        val offsetX = (view.width - cols * scale) / 2
        val offsetY = (view.height - rows * scale) / 2
        val x = ((event.x - offsetX) / scale).toInt()
        val y = ((event.y - offsetY) / scale).toInt()
        if (x < 0 || y < 0 || x >= cols || y >= rows) return false

        val left = (x - SAMPLE_SIZE / 2).coerceIn(0, cols - SAMPLE_SIZE)
        val top = (y - SAMPLE_SIZE / 2).coerceIn(0, rows - SAMPLE_SIZE)
        val sample = rgba.submat(Rect(left, top, SAMPLE_SIZE, SAMPLE_SIZE))
        val mean = Core.mean(sample).`val`
        sample.release()

        val color = Color.rgb(mean[0].toInt(), mean[1].toInt(), mean[2].toInt())

        touchCoordinates.text = "X: $x, Y: $y"
        touchColor.text = String.format("Color: #%06X", 0xFFFFFF and color)
        touchCoordinates.setTextColor(color)
        touchColor.setTextColor(color)
        return false
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "MainActivity"
        private const val CAMERA_REQUEST_CODE = 1
        private const val SAMPLE_SIZE = 8
    }
}
