package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.customgallery.databinding.ItemImageSwipeBinding
import com.likeminds.customgallery.media.model.MediaSwipeViewData
import com.likeminds.customgallery.media.view.adapter.ImageSwipeAdapterListener
import com.likeminds.customgallery.utils.ITEM_IMAGE_SWIPE
import com.likeminds.customgallery.utils.customview.ViewDataBinder

internal class ImageSwipeItemViewDataBinder constructor(val listener: ImageSwipeAdapterListener) :
    ViewDataBinder<ItemImageSwipeBinding, MediaSwipeViewData>() {

    override val viewType: Int
        get() = ITEM_IMAGE_SWIPE

    override fun createBinder(parent: ViewGroup): ItemImageSwipeBinding {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImageSwipeBinding.inflate(inflater, parent, false)
        binding.photoView.setOnClickListener {
            listener.onImageClicked()
        }
        return binding
    }

    override fun bindData(binding: ItemImageSwipeBinding, data: MediaSwipeViewData, position: Int) {
        binding.uri = data.uri().toString()
        listener.onImageViewed()
        binding.executePendingBindings()
    }
}