package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaExtras private constructor(
    val isExternallyShared: Boolean,
    val mediaScreenType: Int,
    val title: String?,
    val subtitle: String?,
    val singleUriData: SingleUriData?,
    val mediaUris: ArrayList<SingleUriData>?,
    val selectedMediaPosition: Int?,
    val conversation: String?,
    val text: String?,
    val position: Int?,
    val medias: List<MediaSwipeViewData>?,
    val cropSquare: Boolean?,
    val downloadableContentTypes: List<String>?,
) : Parcelable {

    internal class Builder {
        private var isExternallyShared: Boolean = false
        private var mediaScreenType: Int = -1
        private var title: String? = null
        private var subtitle: String? = null
        private var singleUriData: SingleUriData? = null
        private var mediaUris: ArrayList<SingleUriData>? = null
        private var selectedMediaPosition: Int? = null
        private var conversation: String? = null
        private var text: String? = null
        private var position: Int? = null
        private var medias: List<MediaSwipeViewData>? = null
        private var cropSquare: Boolean? = null
        private var downloadableContentTypes: List<String>? = null

        fun isExternallyShared(isExternallyShared: Boolean) =
            apply { this.isExternallyShared = isExternallyShared }

        fun mediaScreenType(mediaScreenType: Int) = apply { this.mediaScreenType = mediaScreenType }
        fun title(title: String?) = apply { this.title = title }
        fun subtitle(subtitle: String?) = apply { this.subtitle = subtitle }

        fun singleUriData(singleUriData: SingleUriData?) =
            apply { this.singleUriData = singleUriData }

        fun mediaUris(mediaUris: ArrayList<SingleUriData>?) = apply { this.mediaUris = mediaUris }
        fun selectedMediaPosition(selectedMediaPosition: Int?) =
            apply { this.selectedMediaPosition = selectedMediaPosition }

        fun conversation(conversation: String?) = apply { this.conversation = conversation }
        fun text(text: String?) = apply { this.text = text }
        fun position(position: Int?) = apply { this.position = position }
        fun medias(medias: List<MediaSwipeViewData>?) = apply { this.medias = medias }
        fun cropSquare(cropSquare: Boolean?) = apply { this.cropSquare = cropSquare }
        fun downloadableContentTypes(downloadableContentTypes: List<String>?) =
            apply { this.downloadableContentTypes = downloadableContentTypes }


        fun build() = MediaExtras(
            isExternallyShared,
            mediaScreenType,
            title,
            subtitle,
            singleUriData,
            mediaUris,
            selectedMediaPosition,
            conversation,
            text,
            position,
            medias,
            cropSquare,
            downloadableContentTypes
        )
    }

    fun toBuilder(): Builder {
        return Builder().isExternallyShared(isExternallyShared)
            .mediaScreenType(mediaScreenType)
            .title(title)
            .subtitle(subtitle)
            .singleUriData(singleUriData)
            .mediaUris(mediaUris)
            .selectedMediaPosition(selectedMediaPosition)
            .conversation(conversation)
            .text(text)
            .position(position)
            .medias(medias)
            .cropSquare(cropSquare)
            .downloadableContentTypes(downloadableContentTypes)
    }
}