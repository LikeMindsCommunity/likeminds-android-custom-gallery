package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaPickerItemExtras private constructor(
    val bucketId: String,
    val folderTitle: String,
    val mediaTypes: List<String>,
    val allowMultipleSelect: Boolean,
    val isEditingAllowed: Boolean,
    val text: String?
) : Parcelable {
    internal class Builder {
        private var bucketId: String = ""
        private var folderTitle: String = ""
        private var mediaTypes: List<String> = emptyList()
        private var allowMultipleSelect: Boolean = true
        private var isEditingAllowed: Boolean = false
        private var text: String? = null

        fun bucketId(bucketId: String) = apply { this.bucketId = bucketId }
        fun folderTitle(folderTitle: String) = apply { this.folderTitle = folderTitle }
        fun mediaTypes(mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun allowMultipleSelect(allowMultipleSelect: Boolean) =
            apply { this.allowMultipleSelect = allowMultipleSelect }

        fun isEditingAllowed(isEditingAllowed: Boolean) =
            apply { this.isEditingAllowed = isEditingAllowed }

        fun text(text: String?) = apply { this.text = text }

        fun build() = MediaPickerItemExtras(
            bucketId,
            folderTitle,
            mediaTypes,
            allowMultipleSelect,
            isEditingAllowed,
            text
        )
    }

    fun toBuilder(): Builder {
        return Builder().bucketId(bucketId)
            .folderTitle(folderTitle)
            .mediaTypes(mediaTypes)
            .allowMultipleSelect(allowMultipleSelect)
            .isEditingAllowed(isEditingAllowed)
            .text(text)
    }
}