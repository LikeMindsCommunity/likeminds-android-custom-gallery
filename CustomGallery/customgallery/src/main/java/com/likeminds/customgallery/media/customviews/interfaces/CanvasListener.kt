package com.likeminds.customgallery.media.customviews.interfaces

internal interface CanvasListener {

    fun onDrawStart()

    fun onDrawEnd()

    fun onUndoAvailable(undoAvailable: Boolean)

    fun onCanvasClick()

}