package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.likeminds.customgallery.databinding.ItemSmallAddMediaBinding
import com.likeminds.customgallery.media.model.SmallAddMediaViewData
import com.likeminds.customgallery.media.view.adapter.ImageAdapterListener
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_SMALL_ADD_MEDIA

class SmallAddMediaViewDataBinder constructor(private val imageAdapterListener: ImageAdapterListener) :
    ViewDataBinder<ItemSmallAddMediaBinding, SmallAddMediaViewData>() {

    override val viewType: Int
        get() = ITEM_SMALL_ADD_MEDIA

    override fun createBinder(parent: ViewGroup): ItemSmallAddMediaBinding {
        val binding = ItemSmallAddMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.root.setOnClickListener { imageAdapterListener.onAddMediaClicked() }
        return binding
    }

    override fun bindData(
        binding: ItemSmallAddMediaBinding,
        data: SmallAddMediaViewData,
        position: Int
    ) {
        binding.smallAddMediaViewData = data
        binding.position = position
    }
}