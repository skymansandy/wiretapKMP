package dev.skymansandy.wiretap.helper.launcher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Icon
import androidx.core.graphics.createBitmap

internal object WiretapIconFactory {

    fun createShortcutIcon(sizePx: Int = 108): Icon =
        Icon.createWithBitmap(createShortcutBitmap(sizePx))

    fun createNotificationIcon(sizePx: Int = 24): Icon =
        Icon.createWithBitmap(createNotificationBitmap(sizePx))

    fun createShortcutBitmap(sizePx: Int = 108): Bitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        val scale = sizePx / 108f

        // Background circle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF6200EE.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(54f * scale, 54f * scale, 40f * scale, bgPaint)

        drawWiretapSymbol(canvas, scale)
        return bitmap
    }

    fun createNotificationBitmap(sizePx: Int = 24): Bitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        val scale = sizePx / 108f

        drawWiretapSymbol(canvas, scale)
        return bitmap
    }

    private fun drawWiretapSymbol(canvas: Canvas, scale: Float) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.4f * scale
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        // Up arrow
        val upArrow = Path().apply {
            moveTo(50f * scale, 34f * scale)
            lineTo(43f * scale, 43f * scale)
            lineTo(46.5f * scale, 43f * scale)
            lineTo(46.5f * scale, 51f * scale)
            lineTo(53.5f * scale, 51f * scale)
            lineTo(53.5f * scale, 43f * scale)
            lineTo(57f * scale, 43f * scale)
            close()
        }
        canvas.drawPath(upArrow, fillPaint)

        // Down arrow
        val downArrow = Path().apply {
            moveTo(58f * scale, 74f * scale)
            lineTo(51f * scale, 65f * scale)
            lineTo(54.5f * scale, 65f * scale)
            lineTo(54.5f * scale, 57f * scale)
            lineTo(61.5f * scale, 57f * scale)
            lineTo(61.5f * scale, 65f * scale)
            lineTo(65f * scale, 65f * scale)
            close()
        }
        canvas.drawPath(downArrow, fillPaint)

        // Left curly brace
        val leftBrace = Path().apply {
            moveTo(40f * scale, 32f * scale)
            cubicTo(36f * scale, 32f * scale, 34f * scale, 35f * scale, 34f * scale, 39f * scale)
            lineTo(34f * scale, 50f * scale)
            cubicTo(34f * scale, 52f * scale, 32f * scale, 54f * scale, 30f * scale, 54f * scale)
            cubicTo(32f * scale, 54f * scale, 34f * scale, 56f * scale, 34f * scale, 58f * scale)
            lineTo(34f * scale, 69f * scale)
            cubicTo(34f * scale, 73f * scale, 36f * scale, 76f * scale, 40f * scale, 76f * scale)
        }
        canvas.drawPath(leftBrace, strokePaint)

        // Right curly brace
        val rightBrace = Path().apply {
            moveTo(68f * scale, 32f * scale)
            cubicTo(72f * scale, 32f * scale, 74f * scale, 35f * scale, 74f * scale, 39f * scale)
            lineTo(74f * scale, 50f * scale)
            cubicTo(74f * scale, 52f * scale, 76f * scale, 54f * scale, 78f * scale, 54f * scale)
            cubicTo(76f * scale, 54f * scale, 74f * scale, 56f * scale, 74f * scale, 58f * scale)
            lineTo(74f * scale, 69f * scale)
            cubicTo(74f * scale, 73f * scale, 72f * scale, 76f * scale, 68f * scale, 76f * scale)
        }
        canvas.drawPath(rightBrace, strokePaint)
    }
}
