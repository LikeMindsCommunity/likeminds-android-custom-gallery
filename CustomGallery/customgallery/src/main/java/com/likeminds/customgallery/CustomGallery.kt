package com.likeminds.customgallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.view.MediaPickerActivity

object CustomGallery {
    const val ARG_CUSTOM_GALLERY_RESULT = "custom_gallery_result"

    fun start(
        context: Context,
        customGalleryConfig: CustomGalleryConfig
    ) {
        if (context is AppCompatActivity) {
            initiateMediaPicker(context, customGalleryConfig)
        } else {
            throw IllegalArgumentException("Invalid context!")
        }
    }

    private fun initiateMediaPicker(
        context: AppCompatActivity,
        customGalleryConfig: CustomGalleryConfig
    ) {
        Log.d("PUI", "initiateMediaPicker: ${customGalleryConfig.inputText}")
        val extras = MediaPickerExtras.Builder()
            .mediaTypes(customGalleryConfig.mediaTypes)
            .allowMultipleSelect(customGalleryConfig.allowMultipleSelect)
            .isEditingAllowed(customGalleryConfig.isEditingEnabled)
            .text(customGalleryConfig.inputText)
            .build()

        when (customGalleryConfig.mediaTypes.first()) {
            PDF -> {
                val documentLauncher = getMediaPickerLauncher(context)
                val intent = MediaPickerActivity.getIntent(context, extras)
                documentLauncher.launch(intent)
            }
            AUDIO -> {
                val audioLauncher = getMediaPickerLauncher(context)
                val intent = MediaPickerActivity.getIntent(context, extras)
                audioLauncher.launch(intent)
            }
            else -> {
                val galleryLauncher = getMediaPickerLauncher(context)
                val intent = MediaPickerActivity.getIntent(context, extras)
                galleryLauncher.launch(intent)
            }
        }
    }

    private fun getMediaPickerLauncher(context: AppCompatActivity): ActivityResultLauncher<Intent> {
        Log.d("PUI", "getMediaPickerLauncher: called")
        return context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("PUI", "222: ${result.data.toString()}")
                val data = result.data?.extras?.getParcelable<CustomGalleryResult>(
                    ARG_CUSTOM_GALLERY_RESULT
                )
                    ?: return@registerForActivityResult
                Log.d("PUI", "getMediaPickerLauncher: ${data.medias.first().mediaName}")
            }
        }
    }
}