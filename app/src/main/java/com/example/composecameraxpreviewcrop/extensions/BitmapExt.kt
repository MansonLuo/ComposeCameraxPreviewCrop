package com.example.composecameraxpreviewcrop.extensions

import android.graphics.Bitmap
import android.util.Rational
import kotlin.math.roundToInt

fun Bitmap.cropCenter(
    aspectRatio: Rational,
    screenWidth: Int
): Bitmap? {

//    val newHeight = ((aspectRatio.denominator.toFloat() / aspectRatio.numerator) * width).roundToInt()
//    var cropTop = (height - newHeight) / 2

    val srcRatio: Float = width / height.toFloat()

    return if (aspectRatio.toFloat() > srcRatio) {
        val outputHeight =
            (width / aspectRatio.numerator.toFloat() * aspectRatio.denominator).roundToInt()
        val cropTop = (height - outputHeight) / 2
        Bitmap.createBitmap(this, 0, cropTop, width / 5 * 4, outputHeight)
    } else {
        val outputWidth =
            (height / aspectRatio.denominator.toFloat() * aspectRatio.numerator).roundToInt()
        val cropLeft = (width - outputWidth) / 2
        Bitmap.createBitmap(this, cropLeft, 0, outputWidth, height)
    }
}

fun Bitmap.crop(
    startX: Int,
    startY: Int,
    width: Int,
    height: Int
): Bitmap {
    return Bitmap.createBitmap(this, startX, startY, width, height)
}