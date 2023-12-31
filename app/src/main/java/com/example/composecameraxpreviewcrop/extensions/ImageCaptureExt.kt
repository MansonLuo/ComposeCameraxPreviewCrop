package com.example.composecameraxpreviewcrop.camera

import android.content.Context
import android.media.MediaActionSound
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.transform.CoordinateTransform
import androidx.camera.view.transform.ImageProxyTransformFactory
import androidx.camera.view.transform.OutputTransform
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.example.composecameraxpreviewcrop.extensions.getImageOutputRootDirectory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val PHOTO_EXTENSION = ".jpg"

fun ImageCapture.takePicture(
    context: Context,
    onError: (ImageCaptureException) -> Unit,
    onImageCaptured: (Uri) -> Unit
) {
    val outputDirectory = File(context.getImageOutputRootDirectory())
    val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
    val outputFileOptions = getOutputFileOptions(CameraSelector.LENS_FACING_BACK, photoFile)

    MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)

    this.takePicture(
        outputFileOptions,
        Executors.newSingleThreadExecutor(),
        object: ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val saveUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(saveUri.toFile().extension)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(saveUri.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->

                }

                onImageCaptured(saveUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}


fun createFile(baseFolder: File, format: String, extension: String) =
    File(
        baseFolder, SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension
    )

fun getOutputFileOptions(
    lensFacing: Int,
    photoFile: File
): ImageCapture.OutputFileOptions {
    val metadata = ImageCapture.Metadata().apply {
        isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
        .setMetadata(metadata)
        .build()

    return outputOptions
}