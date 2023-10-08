package com.example.componentialdemo.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class AisseView  constructor(context: Context, attrs: AttributeSet? = null)
    : View(context, attrs) {

    var offsetX: Int = 0
    var offsetY: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        offsetX = measuredWidth / 2
        offsetY = measuredHeight / 2 - 55
    }

    val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var angle = 10.toFloat()

        while (angle < 360) {
            val p = getHeartPoint(angle)
            canvas?.drawPoint(p.x.toFloat(), p.y.toFloat(), paint)
            angle += 0.02f
        }
    }

    fun getHeartPoint(angle: Float): Point {
        val t = Math.toRadians(angle.toDouble())
        val x = (19.5 * (16 * Math.pow(Math.sin(t), 3.0))).toFloat()
        val y = (-20 * (13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t))).toFloat()
        return Point((offsetX + x).toInt(), (offsetY + y).toInt())
    }
}

