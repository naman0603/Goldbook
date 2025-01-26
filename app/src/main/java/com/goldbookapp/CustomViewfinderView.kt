package com.goldbookapp

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.journeyapps.barcodescanner.ViewfinderView

class CustomViewfinderView (context: Context, attrs: AttributeSet) : ViewfinderView(context, attrs) {

    override fun onDraw(canvas: Canvas) {
        refreshSizes()
        if (framingRect == null || framingRect == null) {
            return
        }

        val frame = framingRect

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.color = if (resultBitmap != null) resultColor else maskColor
        canvas.drawRect(0.0f, 0.0f, width, frame!!.top.toFloat(), paint)
        canvas.drawRect(0.0f, frame.top.toFloat(), frame.left.toFloat(), frame.bottom.toFloat() + 1, paint)
        canvas.drawRect(frame.right.toFloat() + 1, frame.top.toFloat(), width, frame.bottom.toFloat() + 1, paint)
        canvas.drawRect(0.0f, frame.bottom.toFloat() + 1, width, height, paint)

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.alpha = CURRENT_POINT_OPACITY
            canvas.drawBitmap(resultBitmap, null, frame, paint)
        }
    }

}