package com.likeminds.customgallery.media.customviews.interfaces

internal fun interface OnProgressVideoListener {
    fun updateProgress(time: Float, max: Float, scale: Float)
}