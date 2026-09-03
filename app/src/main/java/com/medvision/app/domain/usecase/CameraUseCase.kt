package com.medvision.app.domain.usecase

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraUseCase(context: Context) {
    private val cameraExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private val context = context

    suspend fun captureImage(lifecycleOwner: LifecycleOwner): Bitmap? =
        suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val imageCapture = ImageCapture.Builder().build()
                    this.imageCapture = imageCapture

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

                    val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(outputOptions, cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                // Read bitmap from file
                                val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
                                photoFile.delete()
                                continuation.resume(bitmap)
                            }

                            override fun onError(exc: ImageCaptureException) {
                                continuation.resumeWithException(exc)
                            }
                        })
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, android.os.Handler(android.os.Looper.getMainLooper()).looper)
        }
}
