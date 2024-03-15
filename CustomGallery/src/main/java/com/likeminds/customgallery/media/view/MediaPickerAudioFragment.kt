package com.likeminds.customgallery.media.view

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.SimpleItemAnimator
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.FragmentMediaPickerAudioBinding
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.LMMediaPlayer
import com.likeminds.customgallery.media.util.LMMediaPlayer.Companion.handler
import com.likeminds.customgallery.media.util.LMMediaPlayer.Companion.isDataSourceSet
import com.likeminds.customgallery.media.util.LMMediaPlayer.Companion.runnable
import com.likeminds.customgallery.media.util.MediaPlayerListener
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.MediaActivity.Companion.BUNDLE_MEDIA_EXTRAS
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapter
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.media.viewmodel.MediaViewModel
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.ViewUtils
import com.likeminds.customgallery.utils.customview.BaseFragment
import com.likeminds.customgallery.utils.search.CustomSearchBar


internal class MediaPickerAudioFragment :
    BaseFragment<FragmentMediaPickerAudioBinding, MediaViewModel>(),
    MediaPickerAdapterListener, MediaPlayerListener {
    private lateinit var mediaPickerAdapter: MediaPickerAdapter

    private var mediaPlayer: LMMediaPlayer? = null

    private val fragmentActivity by lazy {
        activity as AppCompatActivity?
    }

    companion object {
        const val TAG = "MediaPickerAudio"
        private const val BUNDLE_MEDIA_PICKER_AUDIO = "bundle of media picker audio"

        @JvmStatic
        fun getInstance(extras: MediaPickerExtras): MediaPickerAudioFragment {
            val fragment = MediaPickerAudioFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_MEDIA_PICKER_AUDIO, extras)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getViewModelClass(): Class<MediaViewModel> {
        return MediaViewModel::class.java
    }

    override fun getViewBinding(): FragmentMediaPickerAudioBinding {
        return FragmentMediaPickerAudioBinding.inflate(layoutInflater)
    }

    private lateinit var mediaPickerExtras: MediaPickerExtras
    private val selectedMedias by lazy { HashMap<String, MediaViewData>() }
    private var localItemPosition: Int = 0

    override fun setUpViews() {
        super.setUpViews()
        initializeMediaPlayer()
        initializeUI()
        initializeListeners()
        setHasOptionsMenu(true)
    }

    override fun onProgressChanged(
        playedPercentage: Int,
    ) {
        val item = mediaPickerAdapter.items()?.get(localItemPosition) as MediaViewData

        mediaPickerAdapter.update(
            localItemPosition,
            item.toBuilder()
                .audioProgress(playedPercentage)
                .build()
        )
    }

    override fun observeData() {
        super.observeData()
        viewModel.fetchAllAudioFiles(requireContext()).observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                mediaPickerAdapter.replace(it)
            } else {
                ViewUtils.showShortToast(requireContext(), getString(R.string.no_audio_files))
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun receiveExtras() {
        super.receiveExtras()
        mediaPickerExtras =
            MediaPickerDocumentFragmentArgs.fromBundle(requireArguments()).mediaPickerExtras
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer?.isPlaying() == true) {
            mediaPlayer?.pause()
            val mediaPlayed = mediaPickerAdapter.items()?.get(localItemPosition) as MediaViewData
            handler?.removeCallbacks(runnable ?: Runnable { })
            updateItem(
                localItemPosition, mediaPlayed.toBuilder()
                    .audioProgress(
                        mediaPlayer?.playedPercentage()
                    )
                    .playOrPause(MEDIA_ACTION_PAUSE)
                    .build()
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.media_picker_audio_menu, menu)
        updateMenu(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun updateMenu(menu: Menu) {
        val item = menu.findItem(R.id.menuItemSearch)
        item?.icon?.setTint(Color.BLACK)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuItemSearch -> {
                showSearchToolbar()
            }
            else -> return false
        }
        return true
    }

    override fun onMediaItemClicked(mediaViewData: MediaViewData, itemPosition: Int) {
        if (isMultiSelectionAllowed()) {
            if (selectedMedias.containsKey(mediaViewData.uri.toString())) {
                selectedMedias.remove(mediaViewData.uri.toString())
            } else {
                selectedMedias[mediaViewData.uri.toString()] = mediaViewData
            }

            mediaPickerAdapter.notifyItemChanged(itemPosition)

            updateSelectedCount()
        } else {
            sendSelectedMedias(listOf(mediaViewData))
        }
    }

    override fun isMediaSelectionEnabled(): Boolean {
        return selectedMedias.isNotEmpty()
    }

    override fun isMediaSelected(key: String): Boolean {
        return selectedMedias.containsKey(key)
    }

    override fun isMultiSelectionAllowed(): Boolean {
        return mediaPickerExtras.allowMultipleSelect
    }

    override fun onAudioComplete() {
        super.onAudioComplete()
        handler?.removeCallbacks(runnable ?: Runnable { })
        val playedAudio = mediaPickerAdapter.items()?.get(localItemPosition) as MediaViewData
        updateItem(
            localItemPosition, playedAudio.toBuilder()
                .audioProgress(0)
                .playOrPause(MEDIA_ACTION_NONE)
                .build()
        )
    }

    override fun onAudioActionClicked(mediaViewData: MediaViewData, itemPosition: Int) {
        if (localItemPosition != itemPosition) {
            val previousPlayed =
                mediaPickerAdapter.items()?.get(localItemPosition) as MediaViewData

            if (previousPlayed.playOrPause == MEDIA_ACTION_PLAY) {
                handler?.removeCallbacks(runnable ?: Runnable { })
            }
            updateItem(
                localItemPosition, previousPlayed.toBuilder()
                    .audioProgress(0)
                    .playOrPause(MEDIA_ACTION_NONE)
                    .build()
            )
            isDataSourceSet = false
        }
        try {
            when (mediaViewData.playOrPause) {
                MEDIA_ACTION_PLAY -> {
                    mediaPlayer?.pause()
                    updateItem(
                        itemPosition, mediaViewData.toBuilder()
                            .audioProgress(
                                mediaPlayer?.playedPercentage()
                            )
                            .playOrPause(MEDIA_ACTION_PAUSE)
                            .build()
                    )
                }
                MEDIA_ACTION_PAUSE -> {
                    mediaPlayer?.start()
                    mediaPlayer?.setAudioProgress()
                    updateItem(
                        itemPosition, mediaViewData.toBuilder()
                            .playOrPause(MEDIA_ACTION_PLAY)
                            .build()
                    )
                }
                MEDIA_ACTION_NONE -> {
                    localItemPosition = itemPosition
                    mediaPlayer?.setMediaDataSource(mediaViewData.uri)
                    updateItem(
                        itemPosition, mediaViewData.toBuilder()
                            .playOrPause(MEDIA_ACTION_PLAY)
                            .build()
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.stackTrace.toString())
        }
    }


    private fun initializeMediaPlayer() {
        mediaPlayer = LMMediaPlayer(requireContext(), this)
    }

    override fun doCleanup() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.doCleanup()
    }

    private fun updateItem(position: Int, mediaItem: MediaViewData) {
        mediaPickerAdapter.update(
            position,
            mediaItem
        )
    }

    private fun initializeUI() {
        binding.toolbar.title = ""
        binding.tvToolbarTitle.text = ""

        fragmentActivity?.setSupportActionBar(binding.toolbar)

        initializeTitle()

        mediaPickerAdapter = MediaPickerAdapter(this)
        binding.recyclerView.apply {
            adapter = mediaPickerAdapter
        }
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        handler = Handler(Looper.getMainLooper())
        updateSelectedCount()

        initializeSearchView()
    }

    private fun initializeTitle() {
        binding.tvToolbarTitle.text =
            if (MediaType.isAudio(mediaPickerExtras.mediaTypes)
                && mediaPickerExtras.senderName?.isNotEmpty() == true
            ) {
                String.format("Send to %s", mediaPickerExtras.senderName)
            } else {
                getString(R.string.music)
            }
    }

    private fun updateSelectedCount() {
        if (isMediaSelectionEnabled()) {
            binding.tvToolbarSubtitle.text =
                String.format("%s selected", selectedMedias.size)
        } else {
            binding.tvToolbarSubtitle.text = getString(R.string.tap_to_select)
        }
        binding.fabSend.isVisible = isMediaSelectionEnabled()
    }

    private fun initializeSearchView() {
        val searchBar = binding.searchBar
        searchBar.initialize(lifecycleScope)

        binding.searchBar.setSearchViewListener(
            object : CustomSearchBar.SearchViewListener {
                override fun onSearchViewClosed() {
                    binding.searchBar.visibility = View.GONE
                    viewModel.clearAudioFilter()
                }

                override fun crossClicked() {
                    viewModel.clearAudioFilter()
                }

                override fun keywordEntered(keyword: String) {
                    viewModel.filterAudioByKeyword(keyword)
                }

                override fun emptyKeywordEntered() {
                    viewModel.clearAudioFilter()
                }
            }
        )
        binding.searchBar.observeSearchView(false)
    }

    private fun initializeListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.fabSend.setOnClickListener {
            sendSelectedMedias(selectedMedias.values.toList())
        }
    }

    private fun showSearchToolbar() {
        binding.searchBar.visibility = View.VISIBLE
        binding.searchBar.post {
            binding.searchBar.openSearch()
        }
    }

    private fun sendSelectedMedias(medias: List<MediaViewData>) {
        val mediaUris =
            MediaUtils.convertMediaViewDataToSingleUriData(requireContext(), medias)
        if (mediaUris.isNotEmpty() && mediaPickerExtras.isEditingAllowed) {
            showPickAudioListScreen(mediaUris)
        } else {
            setResultAndFinish(
                mediaUris,
                mediaPickerExtras.text
            )
        }
    }

    private val audioSendLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras?.getParcelable<MediaExtras>(BUNDLE_MEDIA_EXTRAS)
                    ?: return@registerForActivityResult
                setResultAndFinish(
                    data.mediaUris?.toList() ?: listOf(),
                    data.text
                )
            }
        }

    private fun setResultAndFinish(
        mediaUris: List<SingleUriData>,
        text: String?
    ) {
        val resultIntent = CustomGallery.getResultIntent(
            mediaPickerExtras.mediaTypes,
            mediaUris,
            text
        )
        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
        requireActivity().finish()
    }

    private fun showPickAudioListScreen(
        medias: List<SingleUriData>,
        saveInCache: Boolean = false,
        isExternallyShared: Boolean = false,
    ) {
        val attachments = if (saveInCache) {
            AndroidUtil.moveAttachmentToCache(requireContext(), *medias.toTypedArray())
        } else {
            medias
        }

        if (attachments.isNotEmpty()) {
            val arrayList = java.util.ArrayList<SingleUriData>()
            arrayList.addAll(attachments)

            val mediaExtras = MediaExtras.Builder()
                .mediaScreenType(MEDIA_AUDIO_EDIT_SEND_SCREEN)
                .mediaUris(arrayList)
                .text(mediaPickerExtras.text)
                .isExternallyShared(isExternallyShared)
                .build()
            if (attachments.isNotEmpty()) {
                val intent = MediaActivity.getIntent(
                    requireContext(),
                    mediaExtras
                )
                audioSendLauncher.launch(intent)
            }
        }
    }

    private fun clearSelectedMedias() {
        selectedMedias.clear()
        mediaPickerAdapter.notifyDataSetChanged()
        updateSelectedCount()
    }

    fun onBackPress(): Boolean {
        when {
            binding.searchBar.isOpen -> binding.searchBar.closeSearch()
            isMediaSelectionEnabled() -> clearSelectedMedias()
            mediaPlayer?.isPlaying() == true -> {
                mediaPlayer?.stop()
                val previousPlayed =
                    mediaPickerAdapter.items()?.get(localItemPosition) as MediaViewData
                updateItem(
                    localItemPosition, previousPlayed.toBuilder()
                        .audioProgress(0)
                        .playOrPause(MEDIA_ACTION_NONE)
                        .build()
                )
            }
            else -> return true
        }
        return false
    }

}