package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.ItemAudioSmallBinding
import com.likeminds.customgallery.media.model.SmallMediaViewData
import com.likeminds.customgallery.media.view.adapter.ImageAdapterListener
import com.likeminds.customgallery.utils.DateUtil
import com.likeminds.customgallery.utils.ImageBindingUtil
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_AUDIO_SMALL

class SmallAudioViewDataBinder constructor(private val imageAdapterListener: ImageAdapterListener) :
    ViewDataBinder<ItemAudioSmallBinding, BaseViewType>() {

    override val viewType: Int
        get() = ITEM_AUDIO_SMALL

    override fun createBinder(parent: ViewGroup): ItemAudioSmallBinding {
        return ItemAudioSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(binding: ItemAudioSmallBinding, data: BaseViewType, position: Int) {
        val smallMediaData = data as SmallMediaViewData

        binding.smallMediaViewData = smallMediaData

        if (data.isSelected) {
            binding.constraintLayout.background = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.background_transparent_turquoise_2
            )
        } else {
            binding.constraintLayout.background = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.background_transparent
            )
        }

        if (data.singleUriData.thumbnailUri == null) {
            binding.cardNoThumbnail.visibility = View.VISIBLE
            binding.cardThumbnail.visibility = View.GONE
            binding.tvAudioDuration.text =
                DateUtil.formatSeconds(data.singleUriData.duration ?: 0)
        } else {
            binding.cardNoThumbnail.visibility = View.GONE
            binding.cardThumbnail.visibility = View.VISIBLE
            ImageBindingUtil.loadImage(
                binding.ivThumbnail,
                data.singleUriData.thumbnailUri
            )
        }

        binding.root.setOnClickListener {
            imageAdapterListener.mediaSelected(position, smallMediaData)
        }
    }
}