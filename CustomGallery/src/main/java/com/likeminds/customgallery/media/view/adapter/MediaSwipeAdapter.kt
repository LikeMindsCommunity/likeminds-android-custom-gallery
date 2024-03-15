package com.likeminds.customgallery.media.view.adapter

import com.likeminds.customgallery.media.model.MediaSwipeViewData
import com.likeminds.customgallery.media.view.adapter.databinders.ImageSwipeItemViewDataBinder
import com.likeminds.customgallery.media.view.adapter.databinders.VideoSwipeItemViewDataBinder
import com.likeminds.customgallery.utils.customview.BaseRecyclerAdapter
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.BaseViewType

internal class MediaSwipeAdapter constructor(
    val listener: ImageSwipeAdapterListener,
) : BaseRecyclerAdapter<BaseViewType>() {

    init {
        initViewDataBinders()
    }

    override fun getSupportedViewDataBinder(): MutableList<ViewDataBinder<*, *>> {
        val viewDataBinders = ArrayList<ViewDataBinder<*, *>>(2)

        val imageSwipeItemViewDataBinder = ImageSwipeItemViewDataBinder(listener)
        viewDataBinders.add(imageSwipeItemViewDataBinder)

        val videoSwipeItemViewDataBinder = VideoSwipeItemViewDataBinder(listener)
        viewDataBinders.add(videoSwipeItemViewDataBinder)
        return viewDataBinders
    }

}

interface ImageSwipeAdapterListener {
    fun onImageClicked()
    fun onImageViewed() {}
    fun onVideoClicked(mediaSwipeViewData: MediaSwipeViewData)
}