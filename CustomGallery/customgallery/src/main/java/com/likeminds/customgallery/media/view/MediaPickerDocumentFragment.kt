package com.likeminds.customgallery.media.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.likeminds.customgallery.CustomGallery.ARG_CUSTOM_GALLERY_RESULT
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.FragmentMediaPickerDocumentBinding
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.MediaActivity.Companion.BUNDLE_MEDIA_EXTRAS
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapter
import com.likeminds.customgallery.media.view.adapter.MediaPickerAdapterListener
import com.likeminds.customgallery.media.viewmodel.MediaViewModel
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.customview.BaseFragment
import com.likeminds.customgallery.utils.search.CustomSearchBar

internal class MediaPickerDocumentFragment() :
    BaseFragment<FragmentMediaPickerDocumentBinding, MediaViewModel>(),
    MediaPickerAdapterListener {

    private lateinit var mediaPickerAdapter: MediaPickerAdapter

    private val fragmentActivity by lazy { activity as AppCompatActivity? }

    private val selectedMedias by lazy { HashMap<String, MediaViewData>() }
    private lateinit var mediaPickerExtras: MediaPickerExtras

    private var currentSort = SORT_BY_NAME

    private var browseDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onPdfPicked(result.data)
            }
        }

    companion object {
        const val TAG = "MediaPickerDocument"
        private const val BUNDLE_MEDIA_PICKER_DOC = "bundle of media picker doc"

        @JvmStatic
        fun getInstance(extras: MediaPickerExtras): MediaPickerDocumentFragment {
            val fragment = MediaPickerDocumentFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_MEDIA_PICKER_DOC, extras)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getViewModelClass(): Class<MediaViewModel> {
        return MediaViewModel::class.java
    }

    override fun getViewBinding(): FragmentMediaPickerDocumentBinding {
        return FragmentMediaPickerDocumentBinding.inflate(layoutInflater)
    }

    override fun receiveExtras() {
        super.receiveExtras()
        mediaPickerExtras =
            MediaPickerDocumentFragmentArgs.fromBundle(requireArguments()).mediaPickerExtras
    }

    override fun setUpViews() {
        super.setUpViews()
        setHasOptionsMenu(true)
        initializeUI()
        initializeListeners()
        viewModel.fetchAllDocuments(requireContext()).observe(viewLifecycleOwner) {
            mediaPickerAdapter.replace(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.media_picker_document_menu, menu)
        updateMenu(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun updateMenu(menu: Menu) {
        //update search icon
        val item = menu.findItem(R.id.menuItemSearch)
        item?.icon?.setTint(Color.BLACK)

        //update sort icon
        val item2 = menu.findItem(R.id.menuItemSort)
        item2?.icon?.setTint(Color.BLACK)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuItemSearch -> {
                showSearchToolbar()
            }
            R.id.menuItemSort -> {
                val menuItemView = requireActivity().findViewById<View>(item.itemId)
                showSortingPopupMenu(menuItemView)
            }
            else -> return false
        }
        return true
    }

    private fun initializeUI() {
        binding.toolbar.title = ""
        fragmentActivity?.setSupportActionBar(binding.toolbar)

        mediaPickerAdapter = MediaPickerAdapter(this)
        binding.rvDocuments.apply {
            adapter = mediaPickerAdapter
        }

        updateSelectedCount()

        initializeSearchView()
    }

    private fun initializeListeners() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.fabSend.setOnClickListener {
            sendSelectedMedias(selectedMedias.values.toList())
        }
    }

    private fun sendSelectedMedias(medias: List<MediaViewData>) {
        val mediaUris =
            MediaUtils.convertMediaViewDataToSingleUriData(requireContext(), medias)
        if (mediaUris.isNotEmpty() && mediaPickerExtras.isEditingAllowed) {
            showPickDocumentsListScreen(*mediaUris.toTypedArray())
        } else {
            val customGalleryResult = CustomGalleryResult.Builder()
                .medias(mediaUris)
                .text(mediaPickerExtras.text)
                .build()
            val intent = Intent().apply {
                putExtras(Bundle().apply {
                    putParcelable(ARG_CUSTOM_GALLERY_RESULT, customGalleryResult)
                })
            }
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }
    }

    private var documentSendLauncher =
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
                        putParcelable(ARG_CUSTOM_GALLERY_RESULT, customGalleryResult)
                    })
                }
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
            } else if (result?.resultCode == Activity.RESULT_FIRST_USER) {
                requireActivity().finish()
            }
        }

    private fun showPickDocumentsListScreen(
        vararg mediaUris: SingleUriData,
        saveInCache: Boolean = false,
        isExternallyShared: Boolean = false,
    ) {
        val attachments = if (saveInCache) {
            AndroidUtil.moveAttachmentToCache(requireContext(), *mediaUris)
        } else {
            mediaUris.asList()
        }

        val arrayList = ArrayList<SingleUriData>()
        arrayList.addAll(attachments)

        val mediaExtras = MediaExtras.Builder()
            .mediaScreenType(MEDIA_DOCUMENT_SEND_SCREEN)
            .mediaUris(arrayList)
            .text(mediaPickerExtras.text)
            .isExternallyShared(isExternallyShared)
            .build()
        if (attachments.isNotEmpty()) {
            val intent = MediaActivity.getIntent(requireContext(), mediaExtras)
            documentSendLauncher.launch(intent)
        }
    }

    private fun updateSelectedCount() {
        if (isMediaSelectionEnabled()) {
            binding.tvSelectedCount.text =
                String.format("%s selected", selectedMedias.size)
        } else {
            binding.tvSelectedCount.text = getString(R.string.tap_to_select)
        }
        binding.fabSend.isVisible = isMediaSelectionEnabled()
    }

    private fun clearSelectedMedias() {
        selectedMedias.clear()
        mediaPickerAdapter.notifyDataSetChanged()
        updateSelectedCount()
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

    override fun browseDocumentClicked() {
        val intent = AndroidUtil.getExternalDocumentPickerIntent(
            allowMultipleSelect = mediaPickerExtras.allowMultipleSelect
        )
        browseDocumentLauncher.launch(intent)
    }

    override fun isMultiSelectionAllowed(): Boolean {
        return mediaPickerExtras.allowMultipleSelect
    }

    private fun showSortingPopupMenu(view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.document_sort_menu, popup.menu)
        when (currentSort) {
            SORT_BY_NAME ->
                popup.menu.findItem(R.id.menuItemSortName).isChecked = true
            SORT_BY_DATE ->
                popup.menu.findItem(R.id.menuItemSortDate).isChecked = true
        }
        popup.setOnMenuItemClickListener { item ->
            item.isChecked = true
            when (item.itemId) {
                R.id.menuItemSortName -> {
                    if (currentSort != SORT_BY_NAME) {
                        currentSort = SORT_BY_NAME
                        viewModel.sortDocumentsByName()
                    }
                }
                R.id.menuItemSortDate -> {
                    if (currentSort != SORT_BY_DATE) {
                        currentSort = SORT_BY_DATE
                        viewModel.sortDocumentsByDate()
                    }
                }
            }
            true
        }
        popup.show()
    }

    private fun initializeSearchView() {
        val searchBar = binding.searchBar
        searchBar.initialize(lifecycleScope)

        binding.searchBar.setSearchViewListener(
            object : CustomSearchBar.SearchViewListener {
                override fun onSearchViewClosed() {
                    binding.searchBar.visibility = View.GONE
                    viewModel.clearDocumentFilter()
                }

                override fun crossClicked() {
                    viewModel.clearDocumentFilter()
                }

                override fun keywordEntered(keyword: String) {
                    viewModel.filterDocumentsByKeyword(keyword)
                }

                override fun emptyKeywordEntered() {
                    viewModel.clearDocumentFilter()
                }
            }
        )
        binding.searchBar.observeSearchView(false)
    }

    private fun showSearchToolbar() {
        binding.searchBar.visibility = View.VISIBLE
        binding.searchBar.post {
            binding.searchBar.openSearch()
        }
    }

    private fun onPdfPicked(data: Intent?) {
        val uris = MediaUtils.getExternalIntentPickerUris(data)
        viewModel.fetchUriDetails(requireContext(), uris) {
            val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                requireContext(), it
            )
            if (mediaUris.isNotEmpty() && mediaPickerExtras.isEditingAllowed) {
                showPickDocumentsListScreen(*mediaUris.toTypedArray(), saveInCache = true)
            } else {
                val customGalleryResult = CustomGalleryResult.Builder()
                    .medias(mediaUris)
                    .text(mediaPickerExtras.text)
                    .build()
                val intent = Intent().apply {
                    putExtras(Bundle().apply {
                        putParcelable(ARG_CUSTOM_GALLERY_RESULT, customGalleryResult)
                    })
                }
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
            }
        }
    }

    fun onBackPressedFromFragment(): Boolean {
        when {
            binding.searchBar.isOpen -> binding.searchBar.closeSearch()
            isMediaSelectionEnabled() -> clearSelectedMedias()
            else -> return true
        }
        return false
    }
}