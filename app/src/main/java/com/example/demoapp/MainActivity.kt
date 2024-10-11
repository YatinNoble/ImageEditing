package com.example.demoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.demoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isFirstImage = true
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
            binding.photoEditorView.addDynamicEditText()
        }

        binding.btnSave.setOnClickListener {
            saveCombinedImage()
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    if (isFirstImage) {
                        binding.photoEditorView.setImageUri(it)
                        isFirstImage = false
                    } else {
                        binding.photoEditorView.addDynamicImage(it)
                    }
                }
            }
        }

    private fun saveCombinedImage() {
        // Get the bitmap from the PhotoEditorView
        val bitmap = binding.photoEditorView.getBitmap()
        // Save the bitmap to local storage
        val filePath = binding.photoEditorView.saveBitmapToLocal(bitmap)

        if (filePath != null) {
            Toast.makeText(this, "Image saved at: $filePath", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
}