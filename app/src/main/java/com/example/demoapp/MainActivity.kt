package com.example.demoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.demoapp.databinding.ActivityMainBinding
import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageMultiplyBlendFilter

class MainActivity : AppCompatActivity(), EditTextFilterClick {
    private lateinit var binding: ActivityMainBinding
    private var isFirstImage = true
    private lateinit var appCompatEditText: AppCompatEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }

        binding.btnAddText.setOnClickListener {
            binding.photoEditorView.addDynamicEditText(clickSuccess = {
                appCompatEditText = it
                binding.textFilter.visibility = View.VISIBLE
                addOrReplaceFragment(EditTextFilterFragment())
            })
        }

        binding.btnSave.setOnClickListener {
            saveCombinedImage()
        }
    }

    private fun addOrReplaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    if (isFirstImage) {
                        binding.photoEditorView.setImageUri(it)
                        isFirstImage = false
                    } else {
                        binding.photoEditorView.addDynamicImage(it, imageViewClickSuccess = {
                            it.filter = GPUImageBrightnessFilter(2f)

                        })
                    }
                }
            }
        }

    private fun saveCombinedImage() {
        val bitmap = binding.photoEditorView.getBitmap()
        val filePath = binding.photoEditorView.saveBitmapToLocal(bitmap)

        if (filePath != null) {
            Toast.makeText(this, "Image saved at: $filePath", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteViewClicked() {
        binding.photoEditorView.removeEditText(appCompatEditText)
    }

    override fun onTextColorClicked(editText: EditText) {
        editText.text = appCompatEditText.text
        appCompatEditText.setTextColor(ContextCompat.getColor(this, R.color.red))
    }

    override fun onBackgroundColorClicked() {
        appCompatEditText.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
    }

    override fun onEditTextAndSizeClicked(editText: EditText) {
        appCompatEditText.text = editText.text
        appCompatEditText.textSize = 30f
    }
}