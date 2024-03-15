package com.likeminds.customgallery.media.customviews.interfaces

import android.net.Uri
import com.likeminds.customgallery.media.model.VideoTrimExtras

internal interface OnTrimVideoListener {
    fun onVideoRangeChanged()
    fun onTrimStarted()
    fun getResult(uri: Uri, videoTrimExtras: VideoTrimExtras?)
    fun onFailed(videoTrimExtras: VideoTrimExtras?)
    fun cancelAction()
    fun onError(message: String)
}
