package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.likeminds.customgallery.databinding.ItemVideoSwipeBinding
import com.likeminds.customgallery.media.model.MediaSwipeViewData
import com.likeminds.customgallery.media.view.adapter.ImageSwipeAdapterListener
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_VIDEO_SWIPE

internal class VideoSwipeItemViewDataBinder constructor(val listener: ImageSwipeAdapterListener) :
    ViewDataBinder<ItemVideoSwipeBinding, MediaSwipeViewData>() {

    override val viewType: Int
        get() = ITEM_VIDEO_SWIPE

    override fun createBinder(parent: ViewGroup): ItemVideoSwipeBinding {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVideoSwipeBinding.inflate(inflater, parent, false)
        binding.imageView.setOnClickListener {
            val mediaSwipeViewData = binding.mediaSwipeViewData ?: return@setOnClickListener
            listener.onVideoClicked(mediaSwipeViewData)
        }
        return binding
    }

    override fun bindData(binding: ItemVideoSwipeBinding, data: MediaSwipeViewData, position: Int) {
        binding.mediaSwipeViewData = data
        if (!data.thumbnail.isNullOrEmpty()) {
            Glide.with(binding.imageView).load(data.thumbnail).into(binding.imageView)
        } else {
            Glide.with(binding.imageView).load(data.uri).into(binding.imageView)
        }
        binding.executePendingBindings()
    }

}