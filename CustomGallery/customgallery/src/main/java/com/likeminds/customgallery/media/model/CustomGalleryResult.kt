package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class CustomGalleryResult private constructor(
    val medias: List<SingleUriData>,
    val text: String?
) : Parcelable {
    class Builder {
        private var medias: List<SingleUriData> = listOf()
        private var text: String? = null

        fun medias(medias: List<SingleUriData>) = apply { this.medias = medias }
        fun text(text: String?) = apply { this.text = text }

        fun build() = CustomGalleryResult(
            medias,
            text
        )
    }

    fun toBuilder(): Builder {
        return Builder().medias(medias)
            .text(text)
    }
}