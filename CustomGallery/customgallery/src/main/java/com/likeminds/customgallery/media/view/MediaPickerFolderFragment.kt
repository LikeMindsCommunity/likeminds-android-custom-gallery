package com.likeminds.customgallery.media.view

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.navigation.fragment.findNavController
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.FragmentMediaPickerFolderBinding
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapter
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.media.viewmodel.MediaViewModel
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.customview.BaseFragment
import com.likeminds.customgallery.utils.recyclerview.GridSpacingItemDecoration

internal class MediaPickerFolderFragment :
    BaseFragment<FragmentMediaPickerFolderBinding, MediaViewModel>(),
    MediaPickerAdapterListener {

    private lateinit var mediaPickerAdapter: MediaPickerAdapter

    private lateinit var mediaPickerExtras: MediaPickerExtras
    private val appsList by lazy { ArrayList<LocalAppData>() }

    companion object {
        const val BUNDLE_MEDIA_PICKER_FOLDER = "bundle of media picker folder"
        const val TAG = "MediaPickerFolder"

        @JvmStatic
        fun getInstance(extras: MediaPickerExtras): MediaPickerFolderFragment {
            val fragment = MediaPickerFolderFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_MEDIA_PICKER_FOLDER, extras)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getViewModelClass(): Class<MediaViewModel> {
        return MediaViewModel::class.java
    }

    override fun getViewBinding(): FragmentMediaPickerFolderBinding {
        return FragmentMediaPickerFolderBinding.inflate(layoutInflater)
    }

    override fun receiveExtras() {
        super.receiveExtras()
        mediaPickerExtras =
            MediaPickerFolderFragmentArgs.fromBundle(requireArguments()).mediaPickerExtras
        getExternalAppList()
    }

    override fun setUpViews() {
        super.setUpViews()
        setHasOptionsMenu(true)
        initializeUI()
        initializeListeners()
        viewModel.fetchAllFolders(requireContext(), mediaPickerExtras.mediaTypes)
            .observe(viewLifecycleOwner) {
                mediaPickerAdapter.replace(it)
            }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        appsList.forEachIndexed { index, localAppData ->
            menu.add(0, localAppData.appId, index, localAppData.appName)
            menu.getItem(index).icon = localAppData.appIcon
        }
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    private val externalPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = MediaUtils.getExternalIntentPickerUris(result.data)
                viewModel.fetchUriDetails(requireContext(), uris) {
                    val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                        requireContext(), it
                    )
                    if (mediaUris.isNotEmpty() && mediaPickerExtras.isEditingAllowed) {
                        showPickImagesListScreen(
                            mediaUris,
                            saveInCache = true
                        )
                    } else {
                        setResultAndFinish(
                            mediaUris,
                            mediaPickerExtras.text
                        )
                    }
                }
            }
        }

    private var imageVideoSendLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data =
                    result.data?.extras?.getParcelable<MediaExtras>(MediaActivity.BUNDLE_MEDIA_EXTRAS)
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
            mediaUris,
            text
        )
        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
        requireActivity().finish()
    }

    private fun showPickImagesListScreen(
        medias: List<SingleUriData>,
        saveInCache: Boolean = false,
        isExternallyShared: Boolean = false
    ) {
        val attachments = if (saveInCache) {
            AndroidUtil.moveAttachmentToCache(requireContext(), *medias.toTypedArray())
        } else {
            medias
        }
        if (attachments.isNotEmpty()) {
            val arrayList = ArrayList<SingleUriData>()
            arrayList.addAll(attachments)

            val mediaExtras = MediaExtras.Builder()
                .mediaScreenType(MEDIA_EDIT_SCREEN)
                .mediaUris(arrayList)
                .text(mediaPickerExtras.text)
                .isExternallyShared(isExternallyShared)
                .build()

            val intent =
                MediaActivity.getIntent(requireContext(), mediaExtras)
            imageVideoSendLauncher.launch(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val localAppData = appsList.find {
            it.appId == item.itemId
        }

        return if (localAppData != null) {
            val intent = AndroidUtil.getExternalPickerIntent(
                mediaPickerExtras.mediaTypes,
                mediaPickerExtras.allowMultipleSelect,
                Pair(
                    localAppData.resolveInfo.activityInfo.applicationInfo.packageName,
                    localAppData.resolveInfo.activityInfo.name
                )
            )
            externalPickerLauncher.launch(intent)
            true
        } else {
            false
        }
    }

    private fun initializeUI() {
        binding.toolbar.title = ""

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        initializeTitle()

        mediaPickerAdapter = MediaPickerAdapter(this)
        binding.rvFolder.apply {
            addItemDecoration(GridSpacingItemDecoration(2, 12))
            adapter = mediaPickerAdapter
        }
    }

    private fun initializeTitle() {
        binding.tvToolbarTitle.text =
            if (MediaType.isBothImageAndVideo(mediaPickerExtras.mediaTypes)
                && mediaPickerExtras.senderName?.isNotEmpty() == true
            ) {
                String.format("Send to %s", mediaPickerExtras.senderName)
            } else {
                getString(R.string.gallery)
            }
    }

    private fun initializeListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun getExternalAppList() {
        when {
            MediaType.isBothImageAndVideo(mediaPickerExtras.mediaTypes) -> {
                appsList.addAll(AndroidUtil.getExternalMediaPickerApps(requireContext()))
            }
            MediaType.isImage(mediaPickerExtras.mediaTypes) -> {
                appsList.addAll(AndroidUtil.getExternalImagePickerApps(requireContext()))
            }
            MediaType.isVideo(mediaPickerExtras.mediaTypes) -> {
                appsList.addAll(AndroidUtil.getExternalVideoPickerApps(requireContext()))
            }
        }
    }

    override fun onFolderClicked(folderData: MediaFolderViewData) {
        val extras = MediaPickerItemExtras.Builder()
            .bucketId(folderData.bucketId)
            .folderTitle(folderData.title)
            .mediaTypes(mediaPickerExtras.mediaTypes)
            .allowMultipleSelect(mediaPickerExtras.allowMultipleSelect)
            .text(mediaPickerExtras.text)
            .isEditingAllowed(mediaPickerExtras.isEditingAllowed)
            .build()

        findNavController().navigate(
            MediaPickerFolderFragmentDirections.actionFolderToItems(extras)
        )
    }
}