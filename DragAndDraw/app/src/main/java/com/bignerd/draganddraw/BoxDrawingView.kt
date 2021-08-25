package com.bignerd.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.bignerd.draganddraw.Model.Box
import java.util.ArrayList


private const val TAG = "BoxDrawingView"

class BoxDrawingView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var currentBox: Box? = null
    private var boxes = mutableListOf<Box>()
    private val backgroundPaint: Paint = Paint().apply { color = 0xfff8efe0.toInt() }
    private val boxPaint: Paint = Paint().apply { color = 0x22ff0000.toInt() }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState",super.onSaveInstanceState())
        bundle.putParcelableArrayList("boxes",boxes as ArrayList<Box>)
        Log.e(TAG,"Saving ${boxes.size} boxes")
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null) {
            val bandle = state as Bundle
            boxes = bandle.getParcelableArrayList<Box>("boxes") as MutableList<Box>
            Log.e(TAG,"The size of boxes after restore is ${boxes.size}")
            invalidate()
            return super.onRestoreInstanceState(bandle.getParcelable("superState"))
        }
        super.onRestoreInstanceState(state)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        var action = ""

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                action = "ACTION_DOWN"
                currentBox = Box(current).also { boxes.add(it) }
            }

            MotionEvent.ACTION_UP -> {
                action = "ACTION_UP"
                updateCurrentBox(current)
                currentBox = null
            }

            MotionEvent.ACTION_MOVE -> {
                action = "ACTION_MOVE"
                updateCurrentBox(current)
            }

            MotionEvent.ACTION_CANCEL -> {
                action = "ACTION_CANCEL"
                currentBox = null
            }

        }

        //Log.i(TAG, "$action at x=${current.x}, y=${current.y}")
        return true
    }

    private fun updateCurrentBox(current: PointF) {
        currentBox?.let {
            it.end = current
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPaint(backgroundPaint)

        boxes.forEach { box -> canvas.drawRect(box.left, box.top, box.right, box.bottom, boxPaint) }
    }
}