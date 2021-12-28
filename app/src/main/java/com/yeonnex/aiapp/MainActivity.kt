package com.yeonnex.aiapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.yeonnex.aiapp.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class MainActivity : AppCompatActivity() {

    private val TAG = "==================="
    private lateinit var binding: ActivityMainBinding
    private val REQUIRED_PERMISSIONS: String = Manifest.permission.CAMERA
    private val REQUEST_CODE_PERMISSIONS = 10
    private var imageCapture: ImageCapture? = null
    private var outputDirectory: File? = null
    private var cameraExecutor: ExecutorService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            // 카메라 시작
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(REQUIRED_PERMISSIONS), REQUEST_CODE_PERMISSIONS)
        }

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        if (imageCapture == null) { Log.d(TAG, "imageCapture is null...") }
        else Log.d(TAG, "imageCaptue is NOT... NULL....")
        val imageCapture = imageCapture?: return
        val photoFile = File(outputDirectory, SimpleDateFormat(FILENAME_FORMAT,  Locale.KOREA).format(System.currentTimeMills()) + ".jpg")
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOption, ContextCompat.getMainExecutor(this),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                Log.e("=======", "ImageCaptureException ${e.message}")
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                Toast.makeText(baseContext, "success: $savedUri", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startCamera() { // 얘도 지연 요청을 해야 함
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)
        val preview = Preview.Builder().build().also{
            it.setSurfaceProvider(viewFinder.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().build() // 빌더 패턴
        val camaraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try{
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture )
        }catch (e:Exception){ e.printStackTrace() }
    }, ContextCompat.getMainExecutor(this))

}
    private fun bindPreview(camaraProvider: ProcessCameraProvider){
        val preview: Preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }


    private fun getOutputDirectory(): File? {
        return File(externalMediaDirs[0], System.currentTimeMillis().toString() + ".jpg")
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, REQUIRED_PERMISSIONS) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}
