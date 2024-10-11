package com.example.demoapp

import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.animation.doOnEnd
import java.io.File
import java.io.FileOutputStream

class PhotoEditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private var scaleFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector

    // Initialize the ImageView
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
            scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)
            if (scaleFactor < 1.0f) {
                animateResetScale()
            } else {
                imageView.scaleX = scaleFactor
                imageView.scaleY = scaleFactor
            }
            return true
        }
    }

    // Animate the image reset to match parent dimensions
    private fun animateResetScale() {
        val animator = ValueAnimator.ofFloat(scaleFactor, 1.0f).apply {
            duration = 300 // Duration of the animation in milliseconds
            interpolator = OvershootInterpolator(4f) // Bouncy effect
            addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as Float
                imageView.scaleX = animatedValue
                imageView.scaleY = animatedValue
            }
            doOnEnd {
                scaleFactor = 1.0f // Reset scaleFactor to original size
                adjustImageDimensions() // Adjust dimensions to match parent
            }
        }
        animator.start()
    }

    fun addDynamicEditText() {
        // Create a new EditText
        val editText = EditText(context).apply {
            layoutParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    // Center the EditText in the PhotoEditorView initially
                    addRule(CENTER_IN_PARENT)
                }
            hint = "Type here"
            isFocusable = true
            isFocusableInTouchMode = true
            background = null
            // Set touch listener for dragging
            setOnTouchListener(object : OnTouchListener {
                private var dX: Float = 0f
                private var dY: Float = 0f

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Calculate the distance between the touch point and the view's top-left corner
                            dX = event.rawX - v!!.x
                            dY = event.rawY - v.y
                        }

                        MotionEvent.ACTION_MOVE -> {
                            // Calculate the new position
                            val newX = event.rawX - dX
                            val newY = event.rawY - dY

                            // Get the dimensions of the ImageView
                            val imageViewWidth = imageView.width
                            val imageViewHeight = imageView.height

                            // Get the dimensions of the EditText
                            val editTextWidth = v!!.width
                            val editTextHeight = v.height

                            // Check boundaries to prevent EditText from going outside the ImageView
                            if (newX < 0) {
                                v.x = 0f // Don't go left
                            } else if (newX + editTextWidth > imageViewWidth) {
                                v.x = (imageViewWidth - editTextWidth).toFloat() // Don't go right
                            } else {
                                v.x = newX // Valid horizontal position
                            }

                            if (newY < 0) {
                                v.y = 0f // Don't go up
                            } else if (newY + editTextHeight > imageViewHeight) {
                                v.y = (imageViewHeight - editTextHeight).toFloat() // Don't go down
                            } else {
                                v.y = newY // Valid vertical position
                            }
                        }
                    }
                    return true
                }
            })
        }

        // Add the EditText to the RelativeLayout
        addView(editText)

        // Request focus and show the keyboard
        editText.requestFocus()

        // Show the keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }


    // Method to add an image dynamically
    @SuppressLint("ClickableViewAccessibility")
    fun addDynamicImage(uri: Uri) {
        val dynamicImageView = ImageView(context).apply {
            setImageURI(uri)
            scaleType = ImageView.ScaleType.FIT_CENTER

            // Set layout parameters to position in the top-left corner
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply { addRule(CENTER_IN_PARENT) }

            // Set touch listener for dragging
            setOnTouchListener(object : OnTouchListener {
                private var dX: Float = 0f
                private var dY: Float = 0f

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Calculate the distance between the touch point and the view's top-left corner
                            dX = event.rawX - v!!.x
                            dY = event.rawY - v.y
                        }

                        MotionEvent.ACTION_MOVE -> {
                            // Calculate the new position
                            val newX = event.rawX - dX
                            val newY = event.rawY - dY

                            // Get the dimensions of the ImageView
                            val imageViewWidth = imageView.width
                            val imageViewHeight = imageView.height

                            // Get the dimensions of the EditText
                            val editTextWidth = v!!.width
                            val editTextHeight = v.height

                            // Check boundaries to prevent EditText from going outside the ImageView
                            if (newX < 0) {
                                v.x = 0f // Don't go left
                            } else if (newX + editTextWidth > imageViewWidth) {
                                v.x = (imageViewWidth - editTextWidth).toFloat() // Don't go right
                            } else {
                                v.x = newX // Valid horizontal position
                            }

                            if (newY < 0) {
                                v.y = 0f // Don't go up
                            } else if (newY + editTextHeight > imageViewHeight) {
                                v.y = (imageViewHeight - editTextHeight).toFloat() // Don't go down
                            } else {
                                v.y = newY // Valid vertical position
                            }
                        }
                    }
                    return true
                }
            })
        }
        addView(dynamicImageView)
        dynamicImageView.requestFocus()
    }


    // Method to get a Bitmap of the PhotoEditorView's content
    fun getBitmap(): Bitmap {
        // Create a Bitmap with the same dimensions as the view
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw the view onto the canvas
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
