package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaPickerResult private constructor(
    var isResultOk: Boolean,
    @MediaPickerResultType var mediaPickerResultType: Int,
    var mediaTypes: List<String>,
    var medias: List<MediaViewData>?,
    var browseClassName: Pair<String, String>?,
    var allowMultipleSelect: Boolean,
) : Parcelable {
    internal class Builder {
        private var isResultOk: Boolean = false

        @MediaPickerResultType
        private var mediaPickerResultType: Int = MEDIA_RESULT_BROWSE
        private var mediaTypes: List<String> = emptyList()
        private var medias: List<MediaViewData>? = null
        private var browseClassName: Pair<String, String>? = null
        private var allowMultipleSelect: Boolean = false

        fun isResultOk(isResultOk: Boolean) = apply { this.isResultOk = isResultOk }
        fun mediaPickerResultType(@MediaPickerResultType mediaPickerResultType: Int) =
            apply { this.mediaPickerResultType = mediaPickerResultType }

        fun mediaTypes(mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun medias(medias: List<MediaViewData>?) = apply { this.medias = medias }
        fun browseClassName(browseClassName: Pair<String, String>?) =
            apply { this.browseClassName = browseClassName }

        fun allowMultipleSelect(allowMultipleSelect: Boolean) =
            apply { this.allowMultipleSelect = allowMultipleSelect }

        fun build() = MediaPickerResult(
            isResultOk,
            mediaPickerResultType,
            mediaTypes,
            medias,
            browseClassName,
            allowMultipleSelect
        )
    }

    fun toBuilder(): Builder {
        return Builder().isResultOk(isResultOk)
            .mediaPickerResultType(mediaPickerResultType)
            .mediaTypes(mediaTypes)
            .medias(medias)
            .browseClassName(browseClassName)
            .allowMultipleSelect(allowMultipleSelect)
    }
}