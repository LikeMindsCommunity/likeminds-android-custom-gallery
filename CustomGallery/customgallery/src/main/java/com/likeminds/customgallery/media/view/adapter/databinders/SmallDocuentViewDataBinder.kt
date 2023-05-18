package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.ItemDocumentSmallBinding
import com.likeminds.customgallery.media.model.SmallMediaViewData
import com.likeminds.customgallery.media.view.adapter.ImageAdapterListener
import com.likeminds.customgallery.utils.ImageBindingUtil
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_DOCUMENT_SMALL

class SmallDocumentViewDataBinder constructor(private val imageAdapterListener: ImageAdapterListener) :
    ViewDataBinder<ItemDocumentSmallBinding, BaseViewType>() {

    override val viewType: Int
        get() = ITEM_DOCUMENT_SMALL

    override fun createBinder(parent: ViewGroup): ItemDocumentSmallBinding {
        return ItemDocumentSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(binding: ItemDocumentSmallBinding, data: BaseViewType, position: Int) {
        val smallMediaViewData = data as SmallMediaViewData
        binding.smallMediaViewData = smallMediaViewData

        ImageBindingUtil.loadImage(
            binding.imageViewIcon,
            smallMediaViewData.singleUriData.thumbnailUri,
            R.drawable.ic_pdf
        )
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
        binding.root.setOnClickListener {
            imageAdapterListener.mediaSelected(position, smallMediaViewData)
        }
    }
}