package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.ItemMediaPickerDocumentBinding
import com.likeminds.customgallery.media.model.MediaViewData
import com.likeminds.customgallery.media.util.MediaPickerDataBinderUtils.Companion.getFilteredText
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.utils.DateUtil
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_DOCUMENT

internal class MediaPickerDocumentItemViewDataBinder(
    private val listener: MediaPickerAdapterListener,
) : ViewDataBinder<ItemMediaPickerDocumentBinding, MediaViewData>() {

    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_DOCUMENT

    override fun createBinder(parent: ViewGroup): ItemMediaPickerDocumentBinding {
        val binding = ItemMediaPickerDocumentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.root.setOnClickListener {
            val mediaViewData = binding.mediaViewData ?: return@setOnClickListener
            val position = binding.position ?: return@setOnClickListener
            listener.onMediaItemClicked(mediaViewData, position)
        }
        return binding
    }

    override fun bindData(
        binding: ItemMediaPickerDocumentBinding, data: MediaViewData, position: Int,
    ) {
        binding.position = position
        binding.mediaViewData = data
        binding.isSelected = listener.isMediaSelected(data.uri.toString())

        if (data.filteredKeywords.isNullOrEmpty()) {
            binding.tvDocumentName.text = data.mediaName
        } else {
            binding.tvDocumentName.setText(
                getFilteredText(
                    data.mediaName ?: "",
                    data.filteredKeywords,
                    ContextCompat.getColor(binding.root.context, R.color.turquoise),
                ), TextView.BufferType.SPANNABLE
            )
        }

        binding.tvDocumentSize.text = MediaUtils.getFileSizeText(data.size)
        binding.tvDocumentDate.text = DateUtil.createDateFormat("dd/MM/yy", data.date)
    }
}