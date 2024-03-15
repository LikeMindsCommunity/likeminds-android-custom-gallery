package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.ItemMediaPickerFolderBinding
import com.likeminds.customgallery.media.model.MediaFolderType
import com.likeminds.customgallery.media.model.MediaFolderViewData
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_FOLDER

internal class MediaPickerFolderItemViewDataBinder(
    private val listener: MediaPickerAdapterListener,
) : ViewDataBinder<ItemMediaPickerFolderBinding, MediaFolderViewData>() {

    private var glideRequestManager: RequestManager? = null

    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_FOLDER

    override fun createBinder(parent: ViewGroup): ItemMediaPickerFolderBinding {
        val binding = ItemMediaPickerFolderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.root.setOnClickListener {
            val folderData = binding.folderData ?: return@setOnClickListener
            listener.onFolderClicked(folderData)
        }
        glideRequestManager = Glide.with(binding.root)
        return binding
    }

    override fun bindData(
        binding: ItemMediaPickerFolderBinding,
        data: MediaFolderViewData,
        position: Int,
    ) {
        binding.folderData = data
        binding.ivFolderIcon.setImageResource(getFolderIcon(data.folderType))

        glideRequestManager?.load(data.thumbnailUri)
            ?.diskCacheStrategy(DiskCacheStrategy.NONE)
            ?.transition(DrawableTransitionOptions.withCrossFade())
            ?.into(binding.ivThumbnail)
    }

    private fun getFolderIcon(folderType: MediaFolderType): Int {
        return when (folderType) {
            MediaFolderType.CAMERA -> R.drawable.ic_camera_white
            else -> R.drawable.ic_folder
        }
    }
}