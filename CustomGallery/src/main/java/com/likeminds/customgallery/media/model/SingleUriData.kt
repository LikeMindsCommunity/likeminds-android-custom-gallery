package com.likeminds.customgallery.media.model;

import android.net.Uri;
import android.os.Parcelable;
import kotlinx.parcelize.Parcelize

@Parcelize
class SingleUriData private constructor(
    val uri: Uri,
    val fileType: String,
    val width: Int?,
    val height: Int?,
    val thumbnailUri: Uri?,
    val size: Long,
    val mediaName: String?,
    val pdfPageCount: Int?,
    val duration: Int?,
    val localFilePath: String?
) : Parcelable {
    class Builder {
        private var uri: Uri = Uri.parse("")
        private var fileType: String = ""
        private var width: Int? = null
        private var height: Int? = null
        private var thumbnailUri: Uri? = null
        private var size: Long = 0
        private var mediaName: String? = null
        private var pdfPageCount: Int? = null
        private var duration: Int? = null
        private var localFilePath: String? = null

        fun uri(uri: Uri) = apply { this.uri = uri }
        fun fileType(fileType: String) = apply { this.fileType = fileType }
        fun width(width: Int?) = apply { this.width = width }
        fun height(height: Int?) = apply { this.height = height }
        fun thumbnailUri(thumbnailUri: Uri?) = apply { this.thumbnailUri = thumbnailUri }
        fun size(size: Long) = apply { this.size = size }
        fun mediaName(mediaName: String?) = apply { this.mediaName = mediaName }
        fun pdfPageCount(pdfPageCount: Int?) = apply { this.pdfPageCount = pdfPageCount }
        fun duration(duration: Int?) = apply { this.duration = duration }
        fun localFilePath(localFilePath: String?) = apply { this.localFilePath = localFilePath }


        fun build() = SingleUriData(
            uri,
            fileType,
            width,
            height,
            thumbnailUri,
            size,
            mediaName,
            pdfPageCount,
            duration,
            localFilePath
        )
    }

    fun toBuilder(): Builder {
        return Builder().uri(uri)
            .fileType(fileType)
            .width(width)
            .height(height)
            .thumbnailUri(thumbnailUri)
            .size(size)
            .mediaName(mediaName)
            .pdfPageCount(pdfPageCount)
            .duration(duration)
            .localFilePath(localFilePath)
    }
}