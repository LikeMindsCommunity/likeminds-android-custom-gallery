package com.likeminds.customgallery.media.view

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.FragmentAudioEditSendBinding
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.LMMediaPlayer
import com.likeminds.customgallery.media.util.LMMediaPlayer.Companion.handler
import com.likeminds.customgallery.media.util.LMMediaPlayer.Companion.isDataSourceSet
import com.likeminds.customgallery.media.util.LMMediaPlayer.Companion.runnable
import com.likeminds.customgallery.media.util.MediaPlayerListener
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.adapter.ImageAdapter
import com.likeminds.customgallery.media.view.adapter.ImageAdapterListener
import com.likeminds.customgallery.media.viewmodel.MediaViewModel
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.DateUtil
import com.likeminds.customgallery.utils.ImageBindingUtil
import com.likeminds.customgallery.utils.ProgressHelper
import com.likeminds.customgallery.utils.customview.BaseFragment
import com.likeminds.customgallery.utils.model.ITEM_AUDIO_SMALL

internal class AudioSendEditFragment :
    BaseFragment<FragmentAudioEditSendBinding, MediaViewModel>(), ImageAdapterListener,
    MediaPlayerListener {
    override fun getViewModelClass(): Class<MediaViewModel> {
        return MediaViewModel::class.java
    }

    override fun getViewBinding(): FragmentAudioEditSendBinding {
        return FragmentAudioEditSendBinding.inflate(layoutInflater)
    }

    private lateinit var mediaExtras: MediaExtras

    private lateinit var imageAdapter: ImageAdapter

    private var mediaUriList: ArrayList<SingleUriData>? = null
    private var selectedPosition = 0
    private var selectedUri: SingleUriData? = null

    private var mediaPlayer: LMMediaPlayer? = null
    private lateinit var progressAnim: ObjectAnimator

    companion object {
        const val TAG = "ConversationAudioSendEditFragment"
        private const val BUNDLE_CONVERSATION_AUDIO_SEND = "bundle of conversation audio"

        @JvmStatic
        fun getInstance(extras: MediaExtras): AudioSendEditFragment {
            val fragment = AudioSendEditFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_CONVERSATION_AUDIO_SEND, extras)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun receiveExtras() {
        super.receiveExtras()
        if (arguments == null) return
        mediaExtras =
            AudioSendEditFragmentArgs.fromBundle(requireArguments()).mediaExtras
        mediaUriList = mediaExtras.mediaUris
    }

    override fun doCleanup() {
        mediaPlayer?.release()
        mediaPlayer = null
        progressAnim.cancel()
        super.doCleanup()
    }

    override fun setUpViews() {
        super.setUpViews()
        initRecyclerAdapter()
        createThumbnailFromAudio()
        initMediaPlayer()
        initAnimation()
        initListeners()
        checkForExternallyShared()
    }

    private fun initRecyclerAdapter() {
        imageAdapter = ImageAdapter(this)
    }

    private fun initAnimation() {
        progressAnim = ObjectAnimator.ofFloat(binding.wave, "progress", 0F, 100F).apply {
            interpolator = LinearInterpolator()
        }
    }

    private fun initMediaPlayer() {
        mediaPlayer = LMMediaPlayer(requireContext(), this)
        handler = Handler(Looper.getMainLooper())
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.pause()
        if (progressAnim.isRunning) {
            progressAnim.pause()
        }
    }

    override fun observeData() {
        super.observeData()
        viewModel.mediaListUri.observe(viewLifecycleOwner) { medias ->
            val updatedMedias = medias?.mapIndexed { index, singleMediaUri ->
                val isSelected = index == selectedPosition
                val smallMediaViewData = SmallMediaViewData.Builder()
                    .dynamicViewType(ITEM_AUDIO_SMALL)
                    .singleUriData(singleMediaUri)
                    .isSelected(isSelected)
                    .build()
                mediaUriList?.set(index, singleMediaUri)
                if (isSelected) {
                    selectedUri = smallMediaViewData.singleUriData
                }
                smallMediaViewData
            }.orEmpty()
            imageAdapter.replace(updatedMedias)
            initMedia(selectedUri)
        }

        viewModel.audioByteArray.observe(viewLifecycleOwner) { raw ->
            binding.wave.setRawData(raw) {
                progressAnim.start()
            }
        }

        viewModel.updatedUriDataList.observe(viewLifecycleOwner) { dataList ->
            ProgressHelper.hideProgress(binding.progressBar)
            mediaExtras =
                mediaExtras.toBuilder().mediaUris(dataList as ArrayList<SingleUriData>).build()
            mediaUriList = mediaExtras.mediaUris
            initRVForMedia()
        }
    }

    override fun onAudioComplete() {
        super.onAudioComplete()
        if (mediaPlayer != null) {
            isDataSourceSet = false
            handler?.removeCallbacks(runnable ?: Runnable { })
            binding.tvCurrentDuration.text = getString(R.string.start_duration)
            binding.iconAudioPlay.setImageResource(R.drawable.ic_play)
            progressAnim.cancel()
            binding.wave.setRawData(ByteArray(0))
        }
    }

    override fun mediaSelected(position: Int, smallMediaViewData: SmallMediaViewData) {
        if (selectedPosition == position) return
        showUpdatedPositionData(position, smallMediaViewData)
    }

    private fun initListeners() {
        binding.buttonBack.setOnClickListener {
            val intent = Intent()
            activity?.setResult(Activity.RESULT_CANCELED, intent)
            activity?.finish()
        }

        binding.buttonAdd.setOnClickListener {
            val extras = MediaPickerExtras.Builder()
                .senderName(mediaExtras.chatroomName ?: "Chatroom")
                .mediaTypes(listOf(AUDIO))
                .build()
            val intent = MediaPickerActivity.getIntent(
                requireContext(),
                extras
            )
            pickerLauncher.launch(intent)
        }

        binding.buttonSend.setOnClickListener {
            initSendClick()
        }

        binding.buttonDelete.setOnClickListener {
            deleteCurrentMedia()
        }

        binding.iconAudioPlay.setOnClickListener {
            when {
                !isDataSourceSet -> {
                    mediaPlayer?.setMediaDataSource(selectedUri!!.uri)
                    binding.iconAudioPlay.setImageResource(R.drawable.ic_pause)
                    inflateWave(selectedUri!!.uri, selectedUri!!.duration?.toLong() ?: 0L)
                }
                mediaPlayer?.isPlaying() == true -> {
                    mediaPlayer?.pause()
                    binding.iconAudioPlay.setImageResource(R.drawable.ic_play)
                    if (progressAnim.isRunning) {
                        progressAnim.pause()
                    }
                }
                mediaPlayer?.isPlaying() == false -> {
                    mediaPlayer?.start()
                    binding.iconAudioPlay.setImageResource(R.drawable.ic_pause)
                    if (progressAnim.isPaused) {
                        progressAnim.resume()
                    }
                }
            }
        }
    }

    /**
     * Result callback for new audio pick from custom gallery
     * */
    private val pickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.resultCode == Activity.RESULT_OK && result.data != null) {
                val mediaPickerResult =
                    result.data?.extras?.getParcelable<MediaPickerResult>(MediaPickerActivity.ARG_MEDIA_PICKER_RESULT)
                        ?: return@registerForActivityResult
                when (mediaPickerResult.mediaPickerResultType) {
                    MEDIA_RESULT_PICKED -> {
                        val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                            requireContext(), mediaPickerResult.medias
                        )
                        updateMediaUris(mediaUris)
                    }
                }
            }
        }

    private fun initRVForMedia() {
        val mediaUris = mediaUriList.orEmpty()
        if (mediaUris.size == 1) {
            binding.rvMedias.visibility = View.GONE
        } else {
            initMediaDisplayRecyclerView(mediaUris)
        }
        selectedUri = mediaUris[0]
        initMedia(selectedUri)
    }

    private fun initMedia(selectedUri: SingleUriData?) {
        mediaPlayer?.stop()
        mediaPlayer?.clear()
        isDataSourceSet = false
        invalidateDeleteMediaIcon(mediaUriList?.size)
        if (selectedUri?.fileType == AUDIO) {
            if (selectedUri.thumbnailUri != null) {
                binding.gradientView.visibility = View.VISIBLE
                ImageBindingUtil.loadImage(
                    binding.ivAlbumCover,
                    selectedUri.thumbnailUri
                )
            } else {
                binding.gradientView.visibility = View.GONE
                binding.ivAlbumCover.setImageResource(R.color.orange_yellow)
            }
            progressAnim.cancel()
            binding.wave.setRawData(ByteArray(0))
            progressAnim.currentPlayTime = 0
            binding.tvTotalDuration.text =
                DateUtil.formatSeconds(selectedUri.duration ?: 0)
            binding.tvSize.text = MediaUtils.getFileSizeText(selectedUri.size)
            binding.tvAudioName.text = selectedUri.mediaName ?: "Audio"
            binding.iconAudioPlay.setImageResource(R.drawable.ic_play)
        }
    }

    private fun inflateWave(uri: Uri, duration: Long) {
        viewModel.convertUriToByteArray(requireContext(), uri)
        progressAnim.duration = duration * 1000
    }

    override fun onProgressChanged(currentDuration: String) {
        super.onProgressChanged(currentDuration)
        binding.tvCurrentDuration.text = currentDuration
    }

    private fun initMediaDisplayRecyclerView(mediaUris: List<SingleUriData>?) {
        binding.rvMedias.visibility = View.VISIBLE
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.rvMedias.layoutManager = linearLayoutManager
        binding.rvMedias.adapter = imageAdapter
        imageAdapter.replace(mediaUris?.mapIndexed { index, singleUriData ->
            val isSelected = index == 0
            SmallMediaViewData.Builder()
                .dynamicViewType(ITEM_AUDIO_SMALL)
                .singleUriData(singleUriData)
                .isSelected(isSelected)
                .build()
        })
    }

    private fun updateMediaUris(mediaUris: ArrayList<SingleUriData>, saveInCache: Boolean = false) {
        if (mediaUris.isNotEmpty()) {
            if (mediaUriList?.size == 1) {
                initMediaDisplayRecyclerView(mediaExtras.mediaUris)
            }

            if (saveInCache) {
                mediaUriList?.addAll(
                    AndroidUtil.moveAttachmentToCache(requireContext(), *mediaUris.toTypedArray())
                )
            } else {
                mediaUriList?.addAll(mediaUris)
                createThumbnailFromAudio()
            }
            imageAdapter.replace(mediaUriList?.mapIndexed { index, singleMediaUri ->
                val isSelected = index == selectedPosition
                SmallMediaViewData.Builder()
                    .dynamicViewType(ITEM_AUDIO_SMALL)
                    .singleUriData(singleMediaUri)
                    .isSelected(isSelected)
                    .build()
            })
            invalidateDeleteMediaIcon(mediaUriList?.size)
        }
    }

    private fun initSendClick() {
        val text = binding.etConversation.text.trim().toString()
        val extras = mediaExtras.toBuilder()
            .conversation(text)
            .mediaUris(mediaUriList)
            .build()
        val intent = Intent()
        intent.putExtra(
            MediaActivity.BUNDLE_MEDIA_EXTRAS,
            extras
        )

        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    private fun invalidateDeleteMediaIcon(mediaFilesCount: Int?) {
        binding.buttonDelete.isVisible = mediaFilesCount ?: 0 > 1
    }

    private fun deleteCurrentMedia() {
        mediaUriList?.removeAt(selectedPosition)

        if (mediaUriList?.size == 0) {
            binding.buttonBack.performClick()
            return
        } else {
            if (selectedPosition == mediaUriList?.size) {
                selectedPosition -= 1
            }
            val updatedMedias = mediaUriList?.mapIndexed { index, singleMediaUri ->
                val isSelected = index == selectedPosition
                val smallMediaViewData = SmallMediaViewData.Builder()
                    .dynamicViewType(ITEM_AUDIO_SMALL)
                    .singleUriData(singleMediaUri)
                    .isSelected(isSelected)
                    .build()

                if (isSelected) {
                    selectedUri = smallMediaViewData.singleUriData
                }
                smallMediaViewData
            }.orEmpty()
            imageAdapter.replace(updatedMedias)
            binding.rvMedias.isVisible = updatedMedias.size > 1
            initMedia(selectedUri)
        }
    }

    private fun showUpdatedPositionData(position: Int, smallMediaViewData: SmallMediaViewData) {
        selectedPosition = position
        selectedUri = smallMediaViewData.singleUriData
        initMedia(selectedUri)
        val items = imageAdapter.items().map { it as SmallMediaViewData }
        imageAdapter.update(
            selectedPosition,
            smallMediaViewData.toBuilder().isSelected(true).build()
        )
        items.indices.map { index ->
            if (index != selectedPosition) {
                imageAdapter.update(
                    index,
                    items[index].toBuilder().isSelected(false).build()
                )
            }
        }
    }

    private fun createThumbnailFromAudio() {
        viewModel.createThumbnailForAudio(requireContext(), mediaUriList)
    }

    private fun checkForExternallyShared() {
        if (mediaExtras.isExternallyShared) {
            ProgressHelper.showProgress(binding.progressBar, true)
            viewModel.fetchExternallySharedUriData(
                requireContext(),
                mediaExtras.mediaUris!!.map { it.uri }
            )
        } else {
            initRVForMedia()
        }
    }
}