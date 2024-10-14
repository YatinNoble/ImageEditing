package com.example.demoapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import java.io.InputStream

class ImageElementSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var bitmap: Bitmap? = null
    private var dX: Float = 0f
    private var dY: Float = 0f
    private val paint = Paint()

    init {
        holder.addCallback(this)
    }

    fun setImageUri(uri: Uri) {
        // Decode the bitmap from the given URI without scaling
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Set the layout params to match the image dimensions
        bitmap?.let {
            layoutParams = layoutParams.apply {
                width = it.width
                height = it.height
            }
            requestLayout() // Re-request layout to ensure the view is resized
        }

        drawBitmap()
    }

    private fun drawBitmap() {
        if (holder.surface.isValid && bitmap != null) {
            val canvas: Canvas = holder.lockCanvas()
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR) // Clear previous drawing
            canvas.drawBitmap(bitmap!!, 0f, 0f, paint) // Draw the bitmap at (0, 0) with no scaling
            holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawBitmap()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        drawBitmap()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Release resources if needed
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = event.rawX - x
                dY = event.rawY - y
            }

            MotionEvent.ACTION_MOVE -> {
                val newX = event.rawX - dX
                val newY = event.rawY - dY

                val parentView = parent as? ViewGroup
                parentView?.let {
                    val parentWidth = it.width
                    val parentHeight = it.height

                    // Ensure the view stays within bounds
                    if (newX < 0) {
                        x = 0f
                    } else if (newX + width > parentWidth) {
                        x = (parentWidth - width).toFloat()
                    } else {
                        x = newX
                    }

                    if (newY < 0) {
                        y = 0f
                    } else if (newY + height > parentHeight) {
                        y = (parentHeight - height).toFloat()
                    } else {
                        y = newY
                    }
                }
            }
        }
        return true
    }
}
