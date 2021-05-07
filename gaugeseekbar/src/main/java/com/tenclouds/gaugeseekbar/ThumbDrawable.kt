package com.tenclouds.gaugeseekbar

import android.graphics.*
import android.graphics.drawable.Drawable

class ThumbDrawable(thumbColor: Int, thumbOuterColor: Int, val thumbBitmap: Bitmap? = null) : Drawable() {

    private val whitePaint = Paint().apply {
        color = Color.WHITE
        alpha = 255
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val thumbOuterPaint = Paint().apply {
        isAntiAlias = true
        color = thumbOuterColor
//        alpha = 102
    }

    private val thumbInnerPaint = Paint().apply {
        isAntiAlias = true
        color = thumbColor
    }

    override fun draw(canvas: Canvas) {
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val radius = centerX - bounds.left
        canvas.apply {
            drawCircle(centerX, centerY, radius, thumbOuterPaint)
            drawCircle(centerX, centerY, radius * 3 / 4f, thumbInnerPaint)
//            drawCircle(centerX, centerY, 3f, whitePaint)
            thumbBitmap?.let {
                drawBitmap(it, centerX - it.width / 2, centerY - it.height / 2, whitePaint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}