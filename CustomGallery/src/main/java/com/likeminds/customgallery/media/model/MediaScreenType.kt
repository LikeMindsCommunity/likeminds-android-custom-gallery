package com.likeminds.customgallery.media.model

import androidx.annotation.IntDef

const val MEDIA_HORIZONTAL_LIST_SCREEN = 0
const val MEDIA_EDIT_SCREEN = 1
const val MEDIA_CROP_SCREEN = 2
const val MEDIA_VIDEO_PLAY_SCREEN = 3
const val MEDIA_DOCUMENT_SEND_SCREEN = 4
const val MEDIA_AUDIO_EDIT_SEND_SCREEN = 5

@IntDef(
    MEDIA_HORIZONTAL_LIST_SCREEN,
    MEDIA_EDIT_SCREEN,
    MEDIA_CROP_SCREEN,
    MEDIA_VIDEO_PLAY_SCREEN,
    MEDIA_DOCUMENT_SEND_SCREEN,
    MEDIA_AUDIO_EDIT_SEND_SCREEN
)

@Retention(AnnotationRetention.SOURCE)
internal annotation class MediaScreenType