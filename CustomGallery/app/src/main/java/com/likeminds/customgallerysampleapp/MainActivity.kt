package com.likeminds.customgallerysampleapp

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.media.model.CustomGalleryConfig
import com.likeminds.customgallery.media.model.CustomGalleryResult
import com.likeminds.customgallery.media.model.PDF
import com.likeminds.customgallerysampleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras?.getParcelable<CustomGalleryResult>(
                    CustomGallery.ARG_CUSTOM_GALLERY_RESULT
                ) ?: return@registerForActivityResult
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPress.setOnClickListener {
            CustomGallery.start(
                launcher,
                this,
                CustomGalleryConfig.Builder()
                    .mediaTypes(listOf(PDF))
                    .allowMultipleSelect(true)
                    .inputText("sdfasfas")
                    .isEditingEnabled(true)
                    .build()
            )
        }
    }
}