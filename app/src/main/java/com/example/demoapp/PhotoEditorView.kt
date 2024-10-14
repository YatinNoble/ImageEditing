package com.example.demoapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import jp.co.cyberagent.android.gpuimage.GPUImageView
import java.io.File
import java.io.FileOutputStream

class PhotoEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector
    private val imageView = ImageView(context)

    init {
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        // Add the ImageView to the RelativeLayout
        addView(imageView)

        // Initialize the ScaleGestureDetector for zooming
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

        // Apply attributes (optional) for setting default image
        context.theme.obtainStyledAttributes(attrs, R.styleable.PhotoEditorView, 0, 0).apply {
            try {
                val drawableResId = getResourceId(R.styleable.PhotoEditorView_photo_src, 0)
                if (drawableResId != 0) {
                    setImageResource(drawableResId)
                }
            } finally {
                recycle()
            }
        }
    }

    // Method to set an image from a resource ID
    fun setImageResource(resId: Int) {
        imageView.setImageResource(resId)
        adjustImageDimensions()
    }

    fun setImageUri(uri: Uri) {
        imageView.setImageURI(uri)
        adjustImageDimensions()
    }

    // Method to set an image from a Bitmap
    fun setImageBitmap(bitmap: Bitmap?) {
        imageView.setImageBitmap(bitmap)
        adjustImageDimensions()
    }

    private fun adjustImageDimensions() {
        imageView.post {
            val drawable = imageView.drawable ?: return@post
            val bitmap = (drawable as BitmapDrawable).bitmap
            val imageRatio = bitmap.width.toFloat() / bitmap.height
            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()
            val viewRatio = viewWidth / viewHeight

            if (imageRatio > viewRatio) {
                // Image is wider than the view, fit width and adjust height
                val newHeight = (viewWidth / imageRatio).toInt()
                imageView.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    newHeight
                )
            } else {
                // Image is taller than the view, fit height and adjust width
                val newWidth = (viewHeight * imageRatio).toInt()
                imageView.layoutParams = LayoutParams(
                    newWidth,
                    LayoutParams.MATCH_PARENT
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(1f, 3.0f)
            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor
            return true
        }
    }


    fun addDynamicEditText(clickSuccess: (AppCompatEditText) -> Unit) {
        val editText = EditTextElementView(context).apply {
            setOnClickListener {
                clickSuccess(this)
            }
        }
        addView(editText)
        editText.requestFocus() // Request focus immediately
        editText.showKeyboard()
    }

    // Method to remove a specific EditText
    fun removeEditText(appCompatEditText: AppCompatEditText) {
        removeView(appCompatEditText)
    }

    // Method to add an image dynamically
    fun addDynamicImage(uri: Uri, imageViewClickSuccess: (GPUImageView) -> Unit) {
        val imageElementView = ImageElementView(context).apply {
            setImage(
                uri,
                imageView.width,
                imageView.height
            )

            setOnClickListener {
                imageViewClickSuccess(this)
            }
        }
        addView(imageElementView)
        imageElementView.requestFocus()
    }


    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun saveBitmapToLocal(bitmap: Bitmap): String? {
        val fileName = "photo_editor_image_${System.currentTimeMillis()}.png"
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            fileName
        )
        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null
            ) { _, _ ->

            }
            file.absolutePath // Return the path of the saved file

        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null in case of error
        }
    }
}
