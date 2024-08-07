package com.likeminds.customgallerysampleapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallerysampleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras?.getParcelable<CustomGalleryResult>(
                    CustomGallery.ARG_CUSTOM_GALLERY_RESULT
                ) ?: return@registerForActivityResult

                Log.d("PUI", ": ${data.medias.size} ${data.medias[0].uri}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.apply {
            layoutAddImage.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(IMAGE))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutAddVideo.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(VIDEO))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutAttachFiles.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(PDF))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutAttachAudio.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(AUDIO))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutGallery.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(IMAGE, VIDEO))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }
        }
    }
}