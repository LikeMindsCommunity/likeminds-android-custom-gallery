package com.likeminds.customgallery.utils

import android.content.Context
import android.content.Intent
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.utils.file.util.FileUtil

object AndroidUtil {
    fun moveAttachmentToCache(
        context: Context,
        vararg data: SingleUriData
    ): List<SingleUriData> {
        return data.mapNotNull { singleUriData ->
            val uri = when (singleUriData.fileType) {
                IMAGE -> {
                    FileUtil.getSharedImageUri(context, singleUriData.uri)
                }
                VIDEO -> {
                    FileUtil.getSharedVideoUri(context, singleUriData.uri)
                }
                PDF -> {
                    FileUtil.getSharedPdfUri(context, singleUriData.uri)
                }
                else -> null
            }
            if (uri != null) {
                singleUriData.toBuilder().uri(uri).build()
            } else {
                null
            }
        }
    }

    /**
     * Returns the Intent to pick specific mediaTypes files from external storage
     * @param mediaTypes - All the mediaTypes for which intent will be called
     * @param allowMultipleSelect - Specify if multiple media files can be selected
     * @param browseClassName - Specify class package and class name of a specific app which needs to be called
     * */
    fun getExternalPickerIntent(
        mediaTypes: List<String>,
        allowMultipleSelect: Boolean,
        browseClassName: Pair<String, String>?
    ): Intent? {
        val intent = when {
            MediaType.isBothImageAndVideo(mediaTypes) -> {
                getExternalMediaPickerIntent(allowMultipleSelect)
            }
            MediaType.isImage(mediaTypes) -> {
                getExternalImagePickerIntent(allowMultipleSelect)
            }
            MediaType.isVideo(mediaTypes) -> {
                getExternalVideoPickerIntent(allowMultipleSelect)
            }
            else -> null
        }
        if (intent != null && browseClassName != null) {
            intent.setClassName(browseClassName.first, browseClassName.second)
        }
        return intent
    }

    /**
     * Returns the Intent to pick images from external storage
     * */
    private fun getExternalImagePickerIntent(allowMultipleSelect: Boolean = true): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleSelect)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }

    /**
     * Returns the Intent to pick videos from external storage
     * */
    private fun getExternalVideoPickerIntent(allowMultipleSelect: Boolean = true): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleSelect)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }

    /**
     * Returns the Intent to pick both images and videos from external storage
     * */
    private fun getExternalMediaPickerIntent(allowMultipleSelect: Boolean = true): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleSelect)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }

    /**
     * Returns the Intent to pick pdfs from external storage
     * */
    fun getExternalDocumentPickerIntent(allowMultipleSelect: Boolean = true): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultipleSelect)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        return intent
    }
}