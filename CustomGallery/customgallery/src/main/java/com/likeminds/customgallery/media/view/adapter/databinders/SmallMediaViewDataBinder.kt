package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.ItemMediaSmallBinding
import com.likeminds.customgallery.media.model.SingleUriData
import com.likeminds.customgallery.media.model.SmallMediaViewData
import com.likeminds.customgallery.media.model.VIDEO
import com.likeminds.customgallery.media.view.adapter.ImageAdapterListener
import com.likeminds.customgallery.utils.ImageBindingUtil
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_SMALL

class SmallMediaViewDataBinder constructor(private val imageAdapterListener: ImageAdapterListener) :
    ViewDataBinder<ItemMediaSmallBinding, BaseViewType>() {

    override val viewType: Int
        get() = ITEM_MEDIA_SMALL

    override fun createBinder(parent: ViewGroup): ItemMediaSmallBinding {
        val inflater = LayoutInflater.from(parent.context)
        return ItemMediaSmallBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemMediaSmallBinding, data: BaseViewType, position: Int) {
        val smallMediaViewData = data as SmallMediaViewData
        binding.smallMediaViewData = smallMediaViewData
        initMedia(binding, data.singleUriData)

        if (data.isSelected) {
            binding.clImage.background = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.background_transparent_turquoise_2
            )
        } else {
            binding.clImage.background = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.background_transparent
            )
        }
        binding.image.setOnClickListener {
            imageAdapterListener.mediaSelected(position, smallMediaViewData)
        }
    }

    private fun initMedia(binding: ItemMediaSmallBinding, singleUriData: SingleUriData) {
        if (singleUriData.thumbnailUri != null) {
            ImageBindingUtil.loadImage(binding.image, singleUriData.thumbnailUri.toString())
        } else {
            ImageBindingUtil.loadImage(binding.image, singleUriData.uri.toString())
        }
        if (singleUriData.fileType == VIDEO) {
            binding.imageViewVideo.visibility = View.VISIBLE
        } else {
            binding.imageViewVideo.visibility = View.GONE
        }
    }
}