package com.likeminds.customgallery.media.view.adapter

import com.likeminds.customgallery.media.model.SmallMediaViewData
import com.likeminds.customgallery.media.view.adapter.databinders.SmallAddMediaViewDataBinder
import com.likeminds.customgallery.media.view.adapter.databinders.SmallAudioViewDataBinder
import com.likeminds.customgallery.media.view.adapter.databinders.SmallDocumentViewDataBinder
import com.likeminds.customgallery.media.view.adapter.databinders.SmallMediaViewDataBinder
import com.likeminds.customgallery.utils.customview.BaseRecyclerAdapter
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.BaseViewType

class ImageAdapter constructor(
    val listener: ImageAdapterListener,
) : BaseRecyclerAdapter<BaseViewType>() {

    init {
        initViewDataBinders()
    }

    override fun getSupportedViewDataBinder(): MutableList<ViewDataBinder<*, *>> {
        val viewDataBinders = ArrayList<ViewDataBinder<*, *>>(4)

        val smallMediaViewDataBinder = SmallMediaViewDataBinder(listener)
        viewDataBinders.add(smallMediaViewDataBinder)

        val smallDocumentViewDataBinder = SmallDocumentViewDataBinder(listener)
        viewDataBinders.add(smallDocumentViewDataBinder)

        val smallAddMediaViewDataBinder = SmallAddMediaViewDataBinder(listener)
        viewDataBinders.add(smallAddMediaViewDataBinder)

        val smallAudioViewDataBinder = SmallAudioViewDataBinder(listener)
        viewDataBinders.add(smallAudioViewDataBinder)
        return viewDataBinders
    }
}

interface ImageAdapterListener {
    fun mediaSelected(position: Int, smallMediaViewData: SmallMediaViewData)
    fun onAddMediaClicked() {}
}