package com.example.demoapp


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import jp.co.cyberagent.android.gpuimage.GPUImageView
import java.io.InputStream
import kotlin.math.min


class ImageElementView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GPUImageView(context, attrs) {

    private var isDragging = false
    private var onImageViewClickListener: OnImageViewClickListener? = null
    private var onClickListener: View.OnClickListener? = null

    init {
//        scaleType = ScaleType.FIT_CENTER
//        adjustViewBounds = true
        val padding = context.resources.getDimension(R.dimen.size_8).toInt()
        setPadding(padding, padding, padding, padding)
        background = ContextCompat.getDrawable(context, R.drawable.edittext_element_view_bg)


        // Set touch listener for dragging
        setOnTouchListener(object : OnTouchListener {
            private var dX: Float = 0f
            private var dY: Float = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDragging = false
                        dX = event.rawX - v!!.x
                        dY = event.rawY - v.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        isDragging = true
                        val newX = event.rawX - dX
                        val newY = event.rawY - dY

                        // Check boundaries to prevent the view from going outside its parent
                        v?.let {
                            val parentWidth = (it.parent as ViewGroup).width
                            val parentHeight = (it.parent as ViewGroup).height
                            val viewWidth = it.width
                            val viewHeight = it.height

                            it.x = newX.coerceIn(0f, (parentWidth - viewWidth).toFloat())
                            it.y = newY.coerceIn(0f, (parentHeight - viewHeight).toFloat())
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            performClick()
                        }
                    }
                }
                return true
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setImage(uri: Uri, containerWidth: Int, containerHeight: Int) {
//        setImageURI(uri)
        setImage(uri)
        scaleImage(containerWidth, containerHeight, uri)
    }


    private fun scaleImage(containerWidth: Int, containerHeight: Int, uri: Uri) {
//        val drawable = drawable ?: return
//        val bitmap = (drawable as BitmapDrawable).bitmap

        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // Get the original dimensions of the bitmap
        val imageWidth = bitmap.width
        val imageHeight = bitmap.height

        // Determine the width and height to be set
        var scaledWidth = imageWidth
        var scaledHeight = imageHeight

        // Check if the image's width or height exceeds the container's dimensions
        if (imageWidth > containerWidth || imageHeight > containerHeight) {
            // Calculate the scale factors for width and height
            val widthScale = containerWidth.toFloat() / imageWidth
            val heightScale = containerHeight.toFloat() / imageHeight

            // Choose the smaller scale factor to maintain aspect ratio
            val scale = min(widthScale, heightScale)

            // Apply the scaling to the dynamic image
            scaledWidth = (imageWidth * scale).toInt()
            scaledHeight = (imageHeight * scale).toInt()
        }

        // Set the scaled dimensions
        layoutParams = RelativeLayout.LayoutParams(scaledWidth, scaledHeight)

        // Center the image in the container
        x = (containerWidth - scaledWidth).toFloat() / 2
        y = (containerHeight - scaledHeight).toFloat() / 2
    }

    override fun performClick(): Boolean {
        super.performClick()
        onClickListener?.onClick(this)
        onImageViewClickListener?.onImageViewClick(this)
        return true
    }
}
