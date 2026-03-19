package com.example.fmoraga.dictuc.ui.common

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream

/**
 * Vista canvas para captura de firma digital.
 * Reemplaza CaptureSignature.java / CaptureSignature2.java.
 */
class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path   = Path()
    private val paint  = Paint().apply {
        color       = Color.BLACK
        strokeWidth = 5f
        style       = Paint.Style.STROKE
        strokeCap   = Paint.Cap.ROUND
        strokeJoin  = Paint.Join.ROUND
        isAntiAlias = true
    }
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var lastX = 0f
    private var lastY = 0f
    var isEmpty = true
        private set

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap!!)
        canvas?.drawColor(Color.WHITE)
    }

    override fun onDraw(c: Canvas) {
        bitmap?.let { c.drawBitmap(it, 0f, 0f, null) }
        c.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastX = x; lastY = y
                isEmpty = false
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP  -> {
                canvas?.drawPath(path, paint)
                path.reset()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        canvas?.drawColor(Color.WHITE)
        isEmpty = true
        invalidate()
    }

    /** Guarda la firma como PNG y devuelve la ruta del archivo. */
    fun saveToFile(dir: File, fileName: String): String {
        val file = File(dir, fileName)
        FileOutputStream(file).use { out ->
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }
}
