package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaPickerExtras private constructor(
    var senderName: String?,
    @MediaType var mediaTypes: List<String>,
    var allowMultipleSelect: Boolean,
) : Parcelable {

    internal class Builder {
        private var senderName: String? = null

        @MediaType
        private var mediaTypes: List<String> = emptyList()
        var allowMultipleSelect: Boolean = true

        fun senderName(senderName: String?) = apply { this.senderName = senderName }
        fun mediaTypes(@MediaType mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun allowMultipleSelect(allowMultipleSelect: Boolean) =
            apply { this.allowMultipleSelect = allowMultipleSelect }

        fun build() = MediaPickerExtras(senderName, mediaTypes, allowMultipleSelect)
    }

    fun toBuilder(): Builder {
        return Builder().senderName(senderName)
            .allowMultipleSelect(allowMultipleSelect)
            .mediaTypes(mediaTypes)
    }
}