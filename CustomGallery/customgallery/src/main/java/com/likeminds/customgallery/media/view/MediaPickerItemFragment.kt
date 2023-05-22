package com.likeminds.customgallery.media.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.FragmentMediaPickerItemBinding
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.MediaActivity.Companion.BUNDLE_MEDIA_EXTRAS
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapter
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.media.viewmodel.MediaViewModel
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.actionmode.ActionModeCallback
import com.likeminds.customgallery.utils.actionmode.ActionModeListener
import com.likeminds.customgallery.utils.customview.BaseAppCompatActivity
import com.likeminds.customgallery.utils.customview.BaseFragment
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_HEADER
import com.likeminds.customgallery.utils.permissions.Permission
import com.likeminds.customgallery.utils.permissions.PermissionDeniedCallback
import com.likeminds.customgallery.utils.permissions.PermissionManager


internal class MediaPickerItemFragment :
    BaseFragment<FragmentMediaPickerItemBinding, MediaViewModel>(),
    MediaPickerAdapterListener,
    ActionModeListener {

    private var actionModeCallback: ActionModeCallback? = null

    lateinit var mediaPickerAdapter: MediaPickerAdapter

    private lateinit var mediaPickerItemExtras: MediaPickerItemExtras
    private val selectedMedias by lazy { HashMap<String, MediaViewData>() }

    companion object {
        private const val BUNDLE_MEDIA_PICKER_ITEM = "bundle of media picker item"
        const val TAG = "MediaPickerItem"

        @JvmStatic
        fun getInstance(extras: MediaPickerItemExtras): MediaPickerItemFragment {
            val fragment = MediaPickerItemFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_MEDIA_PICKER_ITEM, extras)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getViewModelClass(): Class<MediaViewModel> {
        return MediaViewModel::class.java
    }

    override fun getViewBinding(): FragmentMediaPickerItemBinding {
        return FragmentMediaPickerItemBinding.inflate(layoutInflater)
    }

    override fun receiveExtras() {
        super.receiveExtras()
        mediaPickerItemExtras =
            MediaPickerItemFragmentArgs.fromBundle(requireArguments()).mediaPickerItemExtras
    }

    override fun setUpViews() {
        super.setUpViews()
        if (mediaPickerItemExtras.allowMultipleSelect) {
            setHasOptionsMenu(true)
        }
        initializeUI()
        initializeListeners()
        checkStoragePermission()

        viewModel.fetchMediaInBucket(
            requireContext(),
            mediaPickerItemExtras.bucketId,
            mediaPickerItemExtras.mediaTypes as MutableList<String>
        ).observe(viewLifecycleOwner) {
            mediaPickerAdapter.replace(it)
        }
    }

    private fun checkStoragePermission() {
        PermissionManager.performTaskWithPermission(
            activity as BaseAppCompatActivity,
            { },
            Permission.getStoragePermissionData(),
            showInitialPopup = true,
            showDeniedPopup = true,
            permissionDeniedCallback = object : PermissionDeniedCallback {
                override fun onDeny() {
                    requireActivity().supportFragmentManager.popBackStack()
                }

                override fun onCancel() {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.media_picker_item_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.selectMultiple -> {
                startActionMode()
                true
            }
            else -> false
        }
    }

    private fun initializeUI() {
        binding.toolbar.title = ""
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        binding.tvToolbarTitle.text = mediaPickerItemExtras.folderTitle

        mediaPickerAdapter = MediaPickerAdapter(this)
        binding.rvMediaItem.apply {
            val mLayoutManager = GridLayoutManager(requireContext(), 3)
            mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (mediaPickerAdapter.getItemViewType(position)) {
                        ITEM_MEDIA_PICKER_HEADER -> 3
                        else -> 1
                    }
                }
            }
            layoutManager = mLayoutManager
            adapter = mediaPickerAdapter
        }
    }

    private fun initializeListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun startActionMode() {
        if (actionModeCallback == null) {
            actionModeCallback = ActionModeCallback()
        }
        if (actionModeCallback?.isActionModeEnabled() != true) {
            actionModeCallback?.startActionMode(
                this,
                requireActivity() as AppCompatActivity,
                R.menu.media_picker_actions_menu
            )
        }
        updateActionTitle()
    }

    private fun updateActionTitle() {
        if (selectedMedias.size > 0) {
            actionModeCallback?.updateTitle("${selectedMedias.size} selected")
        } else {
            actionModeCallback?.updateTitle(requireContext().getString(R.string.tap_photo_to_select))
        }
    }

    override fun onActionItemClick(item: MenuItem?) {
        when (item?.itemId) {
            R.id.menu_item_ok -> {
                sendSelectedMedia(selectedMedias.values.toMutableList())
            }
        }
    }

    override fun onActionModeDestroyed() {
        selectedMedias.clear()
        mediaPickerAdapter.notifyDataSetChanged()
    }

    override fun onMediaItemClicked(mediaViewData: MediaViewData, itemPosition: Int) {
        sendSelectedMedia(listOf(mediaViewData))
    }

    override fun onMediaItemLongClicked(mediaViewData: MediaViewData, itemPosition: Int) {
        if (selectedMedias.containsKey(mediaViewData.uri.toString())) {
            selectedMedias.remove(mediaViewData.uri.toString())
        } else {
            selectedMedias[mediaViewData.uri.toString()] = mediaViewData
        }

        mediaPickerAdapter.notifyItemChanged(itemPosition)

        // Invalidate Action Menu Items
        if (selectedMedias.isNotEmpty()) {
            startActionMode()
        } else {
            actionModeCallback?.finishActionMode()
        }
    }

    override fun isMediaSelectionEnabled(): Boolean {
        return actionModeCallback?.isActionModeEnabled() == true
    }

    override fun isMediaSelected(key: String): Boolean {
        return selectedMedias.containsKey(key)
    }

    override fun isMultiSelectionAllowed(): Boolean {
        return mediaPickerItemExtras.allowMultipleSelect
    }

    fun onBackPressedFromFragment() {
        if (isMediaSelectionEnabled()) actionModeCallback?.finishActionMode()
        else findNavController().navigateUp()
    }

    private fun sendSelectedMedia(medias: List<MediaViewData>) {
        val extra = MediaPickerResult.Builder()
            .isResultOk(true)
            .mediaPickerResultType(MEDIA_RESULT_PICKED)
            .mediaTypes(mediaPickerItemExtras.mediaTypes)
            .allowMultipleSelect(mediaPickerItemExtras.allowMultipleSelect)
            .medias(medias)
            .build()
        val mediaUris =
            MediaUtils.convertMediaViewDataToSingleUriData(requireContext(), extra.medias)
        if (mediaUris.isNotEmpty() && mediaPickerItemExtras.isEditingAllowed) {
            showPickImagesListScreen(mediaUris)
        } else {
            val customGalleryResult = CustomGalleryResult.Builder()
                .medias(mediaUris)
                .text(mediaPickerItemExtras.text)
                .build()
            val intent = Intent().apply {
                putExtras(Bundle().apply {
                    putParcelable(CustomGallery.ARG_CUSTOM_GALLERY_RESULT, customGalleryResult)
                })
            }
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }
    }

    private var imageVideoSendLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras?.getParcelable<MediaExtras>(BUNDLE_MEDIA_EXTRAS)
                    ?: return@registerForActivityResult
                val customGalleryResult = CustomGalleryResult.Builder()
                    .medias(data.mediaUris?.toList() ?: listOf())
                    .text(data.text)
                    .build()
                val intent = Intent().apply {
                    putExtras(Bundle().apply {
                        putParcelable(CustomGallery.ARG_CUSTOM_GALLERY_RESULT, customGalleryResult)
                    })
                }
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
            }
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
                .text(mediaPickerItemExtras.text)
                .isExternallyShared(isExternallyShared)
                .build()

            val intent =
                MediaActivity.getIntent(requireContext(), mediaExtras, activity?.intent?.clipData)
            imageVideoSendLauncher.launch(intent)
        }
    }
}