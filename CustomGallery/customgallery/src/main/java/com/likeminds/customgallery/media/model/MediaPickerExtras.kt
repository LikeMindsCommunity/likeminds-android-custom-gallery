package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaPickerExtras private constructor(
    val senderName: String?,
    @MediaType val mediaTypes: List<String>,
    val allowMultipleSelect: Boolean,
    val isEditingAllowed: Boolean,
    val text: String?
) : Parcelable {

    internal class Builder {
        private var senderName: String? = null

        @MediaType
        private var mediaTypes: List<String> = emptyList()
        private var allowMultipleSelect: Boolean = true
        private var isEditingAllowed: Boolean = false
        private var text: String? = null

        fun senderName(senderName: String?) = apply { this.senderName = senderName }
        fun mediaTypes(@MediaType mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun allowMultipleSelect(allowMultipleSelect: Boolean) =
            apply { this.allowMultipleSelect = allowMultipleSelect }

        fun isEditingAllowed(isEditingAllowed: Boolean) =
            apply { this.isEditingAllowed = isEditingAllowed }

        fun text(text: String?) = apply { this.text = text }

        fun build() = MediaPickerExtras(
            senderName,
            mediaTypes,
            allowMultipleSelect,
            isEditingAllowed,
            text
        )
    }

    fun toBuilder(): Builder {
        return Builder().senderName(senderName)
            .allowMultipleSelect(allowMultipleSelect)
            .mediaTypes(mediaTypes)
            .isEditingAllowed(isEditingAllowed)
            .text(text)
    }
}