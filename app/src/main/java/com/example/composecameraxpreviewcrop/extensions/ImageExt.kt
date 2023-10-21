package com.example.composecameraxpreviewcrop.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.util.Rational


fun Image.cropImage(
    rotationDegreee: Int,
    xOffset: Int,
    yOffset: Int,
    cropWidth: Int,
    cropHeigh: Int,
    screenWidth: Int
): Bitmap {

    // convert image to bitmap
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // rotate bitmap
    if (rotationDegreee != 0) {
        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(rotationDegreee.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true)
    }

    // Crop the bitmap
    //bitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, cropWidth, cropHeigh)
    //val nb = Bitmap.createScaledBitmap(bitmap, cropWidth, cropHeigh, true)

    val aspectRatio = Rational(295, 193)
    val bt = bitmap.cropCenter(aspectRatio, screenWidth)


    return bt!!
}