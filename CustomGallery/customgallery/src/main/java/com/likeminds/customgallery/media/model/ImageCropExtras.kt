package com.likeminds.customgallery.media.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal class ImageCropExtras private constructor(
    var singleUriData: SingleUriData?,
    var cropSquare: Boolean,
) : Parcelable {
    internal class Builder {
        private var singleUriData: SingleUriData? = null
        private var cropSquare: Boolean = false

        fun singleUriData(singleUriData: SingleUriData?) =
            apply { this.singleUriData = singleUriData }

        fun cropSquare(cropSquare: Boolean) = apply { this.cropSquare = cropSquare }

        fun build() = ImageCropExtras(singleUriData, cropSquare)
    }
}