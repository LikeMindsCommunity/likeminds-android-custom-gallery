package com.likeminds.customgallery

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.likeminds.customgallery.media.model.AUDIO
import com.likeminds.customgallery.media.model.CustomGalleryConfig
import com.likeminds.customgallery.media.model.MediaPickerExtras
import com.likeminds.customgallery.media.model.PDF
import com.likeminds.customgallery.media.view.MediaPickerActivity

object CustomGallery {
    const val ARG_CUSTOM_GALLERY_RESULT = "custom_gallery_result"

    fun start(
        launcher: ActivityResultLauncher<Intent>,
        context: Context,
        customGalleryConfig: CustomGalleryConfig
    ) {
        initiateMediaPicker(launcher, context, customGalleryConfig)
    }

    private fun initiateMediaPicker(
        launcher: ActivityResultLauncher<Intent>,
        context: Context,
        customGalleryConfig: CustomGalleryConfig
    ) {
        val extras = MediaPickerExtras.Builder()
            .mediaTypes(customGalleryConfig.mediaTypes)
            .allowMultipleSelect(customGalleryConfig.allowMultipleSelect)
            .isEditingAllowed(customGalleryConfig.isEditingEnabled)
            .text(customGalleryConfig.inputText)
            .build()

        when (customGalleryConfig.mediaTypes.first()) {
            PDF -> {
                val intent = MediaPickerActivity.getIntent(context, extras)
                launcher.launch(intent)
            }
            AUDIO -> {
                val intent = MediaPickerActivity.getIntent(context, extras)
                launcher.launch(intent)
            }
            else -> {
                val intent = MediaPickerActivity.getIntent(context, extras)
                launcher.launch(intent)
            }
        }
    }
}