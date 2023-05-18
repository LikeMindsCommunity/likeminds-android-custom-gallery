package com.likeminds.customgallery.media.view.adapter.databinders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.ItemMediaPickerAudioBinding
import com.likeminds.customgallery.media.model.MEDIA_ACTION_NONE
import com.likeminds.customgallery.media.model.MEDIA_ACTION_PAUSE
import com.likeminds.customgallery.media.model.MEDIA_ACTION_PLAY
import com.likeminds.customgallery.media.model.MediaViewData
import com.likeminds.customgallery.media.util.MediaPickerDataBinderUtils.Companion.getFilteredText
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.utils.DateUtil
import com.likeminds.customgallery.utils.ViewUtils.hide
import com.likeminds.customgallery.utils.ViewUtils.show
import com.likeminds.customgallery.utils.customview.ViewDataBinder
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_AUDIO

internal class MediaPickerAudioItemViewDataBinder(
    private val listener: MediaPickerAdapterListener,
) : ViewDataBinder<ItemMediaPickerAudioBinding, MediaViewData>() {

    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_AUDIO

    override fun createBinder(parent: ViewGroup): ItemMediaPickerAudioBinding {
        val binding = ItemMediaPickerAudioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        binding.root.setOnClickListener {
            val mediaViewData = binding.mediaViewData ?: return@setOnClickListener
            val position = binding.position ?: return@setOnClickListener
            listener.onMediaItemClicked(mediaViewData, position)
        }

        binding.ivPlayPause.setOnClickListener {
            val mediaViewData = binding.mediaViewData ?: return@setOnClickListener
            val position = binding.position ?: return@setOnClickListener
            listener.onAudioActionClicked(mediaViewData, position)
        }

        binding.ivPlayStateNone.setOnClickListener {
            val mediaViewData = binding.mediaViewData ?: return@setOnClickListener
            val position = binding.position ?: return@setOnClickListener
            listener.onAudioActionClicked(mediaViewData, position)
        }

        return binding
    }

    override fun bindData(
        binding: ItemMediaPickerAudioBinding,
        data: MediaViewData,
        position: Int,
    ) {
        binding.position = position
        binding.mediaViewData = data
        binding.isSelected = listener.isMediaSelected(data.uri.toString())

        if (data.filteredKeywords.isNullOrEmpty()) {
            binding.tvAudioName.text = data.mediaName
        } else {
            binding.tvAudioName.setText(
                getFilteredText(
                    data.mediaName ?: "",
                    data.filteredKeywords,
                    ContextCompat.getColor(binding.root.context, R.color.turquoise),
                ), TextView.BufferType.SPANNABLE
            )
        }

        when (data.playOrPause) {
            MEDIA_ACTION_NONE -> {
                binding.ivPlayStateNone.show()
                binding.ivPlayPause.hide()
                binding.audioProgressBar.hide()
            }
            MEDIA_ACTION_PLAY -> {
                binding.ivPlayStateNone.hide()
                binding.ivPlayPause.show()
                binding.ivPlayPause.setImageResource(R.drawable.ic_audio_pause)
                binding.audioProgressBar.show()
            }
            MEDIA_ACTION_PAUSE -> {
                binding.ivPlayStateNone.hide()
                binding.ivPlayPause.show()
                binding.ivPlayPause.setImageResource(R.drawable.ic_audio_play)
                binding.audioProgressBar.show()
            }
        }

        binding.tvAudioSize.text = MediaUtils.getFileSizeText(data.size)
        binding.audioProgressBar.progress = data.audioProgress ?: 0
        binding.tvAudioDuration.text =
            DateUtil.formatSeconds(data.duration ?: 0)
    }


}