package com.likeminds.customgallerysampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.media.model.CustomGalleryConfig
import com.likeminds.customgallery.media.model.PDF
import com.likeminds.customgallerysampleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CustomGallery.start(
            this,
            CustomGalleryConfig.Builder()
                .mediaTypes(listOf(PDF))
                .allowMultipleSelect(true)
                .inputText("sdfasfas")
                .isEditingEnabled(true)
                .build()
        )

        binding.btnPress.setOnClickListener {
//            CustomGallery.start(
//                this,
//                CustomGalleryConfig.Builder()
//                    .mediaTypes(listOf(IMAGE, VIDEO))
//                    .allowMultipleSelect(true)
//                    .inputText("sdfasfas")
//                    .build()
//            )
        }
    }
}