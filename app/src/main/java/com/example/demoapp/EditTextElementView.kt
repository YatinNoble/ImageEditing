package com.example.demoapp

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import java.util.UUID

@SuppressLint("ClickableViewAccessibility")
class EditTextElementView(context: Context) : AppCompatEditText(context) {

    private var dX: Float = 0f
    private var dY: Float = 0f
    private var isDragging = false
    private var clickListener: OnEditTextClickListener? = null
    private var onClickListener: View.OnClickListener? = null

    init {
        layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.CENTER_IN_PARENT)
        }
        hint = "Type here"
        includeFontPadding = false
        val lestPadding = context.resources.getDimension(R.dimen.size_8).toInt()
        setPadding(lestPadding, lestPadding, lestPadding, lestPadding)
        gravity = Gravity.CENTER
        background = ContextCompat.getDrawable(context, R.drawable.edittext_element_view_bg)

        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Reset dragging state
                    isDragging = false
                    dX = event.rawX - v!!.x
                    dY = event.rawY - v.y
                }

                MotionEvent.ACTION_MOVE -> {
                    isDragging = true // Start dragging
                    val newX = event.rawX - dX
                    val newY = event.rawY - dY

                    val parentView = v.parent as RelativeLayout
                    val imageViewWidth = parentView.width
                    val imageViewHeight = parentView.height

                    if (newX < 0) {
                        v.x = 0f
                    } else if (newX + v.width > imageViewWidth) {
                        v.x = (imageViewWidth - v.width).toFloat()
                    } else {
                        v.x = newX
                    }

                    if (newY < 0) {
                        v.y = 0f
                    } else if (newY + v.height > imageViewHeight) {
                        v.y = (imageViewHeight - v.height).toFloat()
                    } else {
                        v.y = newY
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        performClick()
                    }
                }
            }
            true
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        onClickListener?.onClick(this)
        clickListener?.onEditTextClick(this)
        return true
    }


    fun showKeyboard() {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            showKeyboard()
        }
        return super.onTouchEvent(event)
    }
}
