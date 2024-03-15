package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class CustomGalleryResult private constructor(
    @MediaType var mediaTypes: List<String>,
    val medias: List<SingleUriData>,
    val text: String?,
) : Parcelable {
    class Builder {
        private var mediaTypes: List<String> = listOf()
        private var medias: List<SingleUriData> = listOf()
        private var text: String? = null

        fun mediaTypes(@MediaType mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun medias(medias: List<SingleUriData>) = apply { this.medias = medias }
        fun text(text: String?) = apply { this.text = text }

        fun build() = CustomGalleryResult(
            mediaTypes,
            medias,
            text
        )
    }

    fun toBuilder(): Builder {
        return Builder().medias(medias)
            .mediaTypes(mediaTypes)
            .text(text)
    }
}