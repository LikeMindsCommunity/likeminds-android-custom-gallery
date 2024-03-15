package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.customgallery.databinding.ItemMediaPickerBrowseBinding
import com.likeminds.customgallery.media.model.MediaBrowserViewData
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_BROWSE

internal class MediaPickerBrowseItemViewDataBinder(
    private val listener: MediaPickerAdapterListener,
) : ViewDataBinder<ItemMediaPickerBrowseBinding, MediaBrowserViewData>() {

    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_BROWSE

    override fun createBinder(parent: ViewGroup): ItemMediaPickerBrowseBinding {
        val binding = ItemMediaPickerBrowseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.root.setOnClickListener {
            listener.browseDocumentClicked()
        }
        return binding
    }

    override fun bindData(
        binding: ItemMediaPickerBrowseBinding, data: MediaBrowserViewData, position: Int,
    ) {
    }
}