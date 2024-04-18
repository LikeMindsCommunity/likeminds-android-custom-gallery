package com.likeminds.customgallery.media.model

import androidx.annotation.StringDef
import java.io.File

const val IMAGE = "image"
const val VIDEO = "video"
const val AUDIO = "audio"
const val PDF = "pdf"
const val GIF = "gif"

@StringDef(
    IMAGE, VIDEO, PDF, AUDIO, GIF
)

@Retention(AnnotationRetention.SOURCE)
annotation class MediaType {
    companion object {

        fun getFileType(type: List<String>): String {
            return when {
                isImage(type) -> {
                    IMAGE
                }
                isVideo(type) -> {
                    VIDEO
                }
                isBothImageAndVideo(type) -> {
                    "$IMAGE/$VIDEO"
                }
                isPDF(type) -> {
                    PDF
                }
                isAudio(type) -> {
                    AUDIO
                }
                else -> {
                    ""
                }
            }
        }

        fun contains(type: String): Boolean {
            return type == IMAGE ||
                    type == VIDEO ||
                    type == AUDIO ||
                    type == PDF ||
                    type == GIF
        }

        fun isImage(mediaType: String?): Boolean {
            return mediaType == IMAGE
        }

        fun isImage(mediaTypes: List<String>): Boolean {
            return mediaTypes.contains(IMAGE)
        }

        fun isVideo(mediaType: String?): Boolean {
            return mediaType == VIDEO
        }

        fun isVideo(mediaTypes: List<String>): Boolean {
            return mediaTypes.contains(VIDEO)
        }

        fun isPDF(mediaType: String?): Boolean {
            return mediaType == PDF
        }

        fun isPDF(mediaTypes: List<String>): Boolean {
            return mediaTypes.contains(PDF)
        }

        fun isAudio(mediaType: String?): Boolean {
            return mediaType == AUDIO
        }

        fun isAudio(mediaTypes: List<String>): Boolean {
            return mediaTypes.contains(AUDIO)
        }

        fun isBothImageAndVideo(mediaTypes: List<String>): Boolean {
            return mediaTypes.contains(IMAGE) && mediaTypes.contains(VIDEO)
        }

        fun isImageOrVideo(mediaTypes: List<String>): Boolean {
            return mediaTypes.contains(IMAGE) || mediaTypes.contains(VIDEO)
        }

        fun getMediaFileInitial(mediaType: String?, isThumbnail: Boolean = false): String {
            var initial = when (mediaType) {
                IMAGE -> "IMG_"
                VIDEO -> "VID_"
                PDF -> "DOC_"
                AUDIO -> "AUD_"
                GIF -> "GIF_"
                else -> "MEDIA_"
            }
            if (isThumbnail) {
                initial += "THUMB_"
            }
            return initial
        }

        fun getMediaFileExtension(
            mediaType: String?,
            file: File,
            isThumbnail: Boolean = false
        ): String {
            var extension = file.extension
            if (extension.isEmpty()) {
                extension = if (isThumbnail) {
                    if (mediaType == GIF) "png" else "jpg"
                } else {
                    when (mediaType) {
                        IMAGE -> "jpg"
                        VIDEO -> "mp4"
                        PDF -> "pdf"
                        GIF -> "gif"
                        else -> ""
                    }
                }
            }
            return extension
        }
    }
}