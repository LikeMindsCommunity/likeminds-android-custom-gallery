package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.customgallery.databinding.ItemMediaPickerHeaderBinding
import com.likeminds.customgallery.media.model.MediaHeaderViewData
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_HEADER

internal class MediaPickerHeaderItemViewDataBinder :
    ViewDataBinder<ItemMediaPickerHeaderBinding, MediaHeaderViewData>() {

    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_HEADER

    override fun createBinder(parent: ViewGroup): ItemMediaPickerHeaderBinding {
        return ItemMediaPickerHeaderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    }

    override fun bindData(
        binding: ItemMediaPickerHeaderBinding, data: MediaHeaderViewData, position: Int,
    ) {
        binding.tvHeader.text = data.title
    }
}