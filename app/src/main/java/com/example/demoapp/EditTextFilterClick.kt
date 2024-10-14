package com.example.demoapp

import android.widget.EditText

interface EditTextFilterClick {

    fun onDeleteViewClicked()
    fun onTextColorClicked(editText: EditText)
    fun onBackgroundColorClicked()
    fun onEditTextAndSizeClicked(editText: EditText)
}