package com.example.demoapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.demoapp.databinding.FragmentEditTextFilterBinding


class EditTextFilterFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentEditTextFilterBinding

    private var listener: EditTextFilterClick? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditTextFilterBinding.inflate(layoutInflater, container, false)
        val view = binding.root


        binding.btnDeleteView.setOnClickListener(this)
        binding.btnTextColor.setOnClickListener(this)
        binding.btnBackgroundColor.setOnClickListener(this)
        binding.btnEditTextAndSize.setOnClickListener(this)

        return view
    }

    // Attach the listener when the fragment is attached to the activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EditTextFilterClick) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnButtonClickListener")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnDeleteView -> {
                listener?.onDeleteViewClicked()
            }

            R.id.btnTextColor -> {
                listener?.onTextColorClicked(binding.edtText)
            }

            R.id.btnBackgroundColor -> {
                listener?.onBackgroundColorClicked()
            }

            R.id.btnEditTextAndSize -> {
                listener?.onEditTextAndSizeClicked(binding.edtText)
            }
        }
    }
}

