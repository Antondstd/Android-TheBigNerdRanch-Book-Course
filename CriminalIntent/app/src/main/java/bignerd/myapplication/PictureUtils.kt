package bignerd.myapplication

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    var options = BitmapFactory.Options()
    var srcWidth: Float
    var srcHeight: Float
    var inSampleSize = 1

    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    srcWidth = options.outWidth.toFloat()
    srcHeight = options.outHeight.toFloat()

    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth

        val sampleScale = if (heightScale > widthScale)
            heightScale
        else
            widthScale
        inSampleSize = Math.round(sampleScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    return BitmapFactory.decodeFile(path,options)
}

fun getScaledBitmap(path: String,activity: Activity):Bitmap{
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)

    return getScaledBitmap(path,size.x,size.y)
}