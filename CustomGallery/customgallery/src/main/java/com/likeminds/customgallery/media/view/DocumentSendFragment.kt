package com.likeminds.customgallery.media.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.likeminds.customgallery.R
import com.likeminds.customgallery.databinding.FragmentDocumentSendBinding
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.MediaActivity.Companion.BUNDLE_MEDIA_EXTRAS
import com.likeminds.customgallery.media.view.adapter.ImageAdapter
import com.likeminds.customgallery.media.view.adapter.ImageAdapterListener
import com.likeminds.customgallery.media.viewmodel.MediaViewModel
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.ProgressHelper
import com.likeminds.customgallery.utils.ViewUtils
import com.likeminds.customgallery.utils.ViewUtils.hide
import com.likeminds.customgallery.utils.ViewUtils.show
import com.likeminds.customgallery.utils.customview.BaseFragment
import com.likeminds.customgallery.utils.model.ITEM_DOCUMENT_SMALL

internal class DocumentSendFragment :
    BaseFragment<FragmentDocumentSendBinding, MediaViewModel>(),
    ImageAdapterListener {

    private lateinit var mediaExtras: MediaExtras
    private lateinit var documentURIs: ArrayList<SingleUriData>

    private lateinit var imageAdapter: ImageAdapter

    private var selectedPosition = 0
    private var selectedUri: SingleUriData? = null

    companion object {
        private const val TAG = "DocumentSendFragment"

        private const val BUNDLE_CONVERSATION_DOCUMENT_SEND = "bundle of document edit"

        @JvmStatic
        fun getInstance(extras: MediaExtras): DocumentSendFragment {
            val fragment = DocumentSendFragment()
            val bundle = Bundle()
            bundle.putParcelable(BUNDLE_CONVERSATION_DOCUMENT_SEND, extras)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getViewModelClass(): Class<MediaViewModel>? {
        return MediaViewModel::class.java
    }

    override fun getViewBinding(): FragmentDocumentSendBinding {
        return FragmentDocumentSendBinding.inflate(layoutInflater)
    }

    override fun receiveExtras() {
        super.receiveExtras()
        mediaExtras = MediaEditFragmentArgs.fromBundle(requireArguments()).mediaExtras
        val mediaUris = mediaExtras.mediaUris
        documentURIs = mediaUris as ArrayList<SingleUriData>
    }

    override fun setUpViews() {
        super.setUpViews()
        if (mediaExtras.isExternallyShared) {
            ProgressHelper.showProgress(binding.progressBar, true)
            viewModel.fetchExternallySharedUriData(
                requireContext(),
                documentURIs.map { it.uri })
        } else {
            initRVForMedias(false)
        }

        binding.buttonBack.setOnClickListener {
            val intent = Intent()
            activity?.setResult(Activity.RESULT_CANCELED, intent)
            activity?.finish()
        }

        binding.buttonAdd.setOnClickListener {
            val extras = MediaPickerExtras.Builder()
                .senderName(mediaExtras.chatroomName ?: "Chatroom")
                .mediaTypes(listOf(PDF))
                .build()

            val intent = MediaPickerActivity.getIntent(requireContext(), extras)
            pickerLauncher.launch(intent)
        }

        binding.buttonSend.setOnClickListener {
            initSendClick()
        }

        binding.buttonDelete.setOnClickListener {
            deleteCurrentMedia()
        }
    }

    override fun observeData() {
        super.observeData()

        viewModel.updatedUriDataList.observe(viewLifecycleOwner) { dataList ->
            ProgressHelper.hideProgress(binding.progressBar)
            documentURIs.clear()
            documentURIs.addAll(dataList)
            initRVForMedias(true)
        }

        viewModel.getDocumentPreview().observe(viewLifecycleOwner) { uris ->
            ProgressHelper.hideProgress(binding.progressBar)
            selectedUri = if (documentURIs.size == 1) {
                val uri = uris.firstOrNull()
                if (uri != null) {
                    documentURIs.clear()
                    documentURIs.addAll(uris)
                }
                documentURIs.first()
            } else {
                val adapterItems = imageAdapter.items().filterIsInstance<SmallMediaViewData>()
                    .map { smallViewData ->
                        val uri = uris.firstOrNull { uriData ->
                            smallViewData.singleUriData.uri == uriData.uri
                        } ?: smallViewData.singleUriData
                        smallViewData.toBuilder().singleUriData(uri).build()
                    }
                imageAdapter.replace(adapterItems)
                documentURIs.clear()
                documentURIs.addAll(adapterItems.map { it.singleUriData })
                documentURIs[selectedPosition]
            }
            initMedia(selectedUri)
        }

    }

    //result callback for new document pick from custom gallery
    private val pickerLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result?.resultCode == Activity.RESULT_OK && result.data != null) {
            val mediaPickerResult =
                result.data?.extras?.getParcelable<MediaPickerResult>(MediaPickerActivity.ARG_MEDIA_PICKER_RESULT)
                    ?: return@registerForActivityResult
            when (mediaPickerResult.mediaPickerResultType) {
                MEDIA_RESULT_BROWSE -> {
                    val intent = AndroidUtil.getExternalDocumentPickerIntent(
                        allowMultipleSelect = mediaPickerResult.allowMultipleSelect
                    )
                    browsePickerLauncher.launch(intent)
                }
                MEDIA_RESULT_PICKED -> {
                    val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                        requireContext(), mediaPickerResult.medias
                    )
                    updateMediaUris(mediaUris)
                }
            }
        }
    }

    private fun initMedia(singleUriData: SingleUriData?) {
        if (singleUriData == null) {
            return
        }
        invalidateDeleteMediaIcon(documentURIs.size)
        if (singleUriData.fileType == PDF) {
            val name = singleUriData.mediaName
            val pageCount = singleUriData.pdfPageCount ?: 0
            val size = singleUriData.size ?: 0
            val thumbnail = singleUriData.thumbnailUri

            binding.textViewDocumentName.text = name
            if (pageCount > 0) {
                binding.textViewDocumentPageCount.show()
                binding.viewDotPageCount.show()
                binding.textViewDocumentPageCount.text =
                    getString(R.string.placeholder_pages, pageCount)
            } else {
                binding.textViewDocumentPageCount.hide()
                binding.viewDotPageCount.hide()
            }
            if (size > 0) {
                binding.textViewDocumentSize.show()
                binding.viewDotSize.show()
                binding.textViewDocumentSize.text = MediaUtils.getFileSizeText(size)
            } else {
                binding.textViewDocumentSize.hide()
                binding.viewDotSize.hide()
            }
            configureMetaDataView(thumbnail != null)
            if (thumbnail != null) {
                binding.imageViewDocumentIcon.setImageURI(singleUriData.thumbnailUri)
            }
        }
    }

    /**
     * Based on the document thumbnail, reconfigure the UI
     */
    private fun configureMetaDataView(hasThumbnail: Boolean) {
        val set = ConstraintSet()
        set.clone(binding.constraintLayout)
        if (hasThumbnail) {
            set.connect(
                binding.imageViewDocumentIcon.id,
                ConstraintSet.BOTTOM,
                binding.textViewDocumentName.id,
                ConstraintSet.TOP
            )
            set.connect(
                binding.textViewDocumentName.id,
                ConstraintSet.BOTTOM,
                binding.textViewDocumentSize.id,
                ConstraintSet.TOP
            )
            set.clear(binding.textViewDocumentName.id, ConstraintSet.TOP)
            set.setMargin(
                binding.textViewDocumentName.id,
                ConstraintSet.BOTTOM,
                ViewUtils.dpToPx(6)
            )
            set.connect(
                binding.textViewDocumentSize.id,
                ConstraintSet.BOTTOM,
                binding.buttonSend.id,
                ConstraintSet.TOP
            )
            set.clear(binding.textViewDocumentSize.id, ConstraintSet.TOP)
            set.setMargin(
                binding.textViewDocumentSize.id,
                ConstraintSet.BOTTOM,
                ViewUtils.dpToPx(16)
            )
        } else {
            set.connect(
                binding.imageViewDocumentIcon.id,
                ConstraintSet.BOTTOM,
                binding.bottomView.id,
                ConstraintSet.TOP
            )
            set.connect(
                binding.textViewDocumentName.id,
                ConstraintSet.TOP,
                binding.imageViewDocumentIcon.id,
                ConstraintSet.BOTTOM,
                1
            )
            set.setMargin(binding.textViewDocumentName.id, ConstraintSet.TOP, ViewUtils.dpToPx(16))
            set.clear(binding.textViewDocumentName.id, ConstraintSet.BOTTOM)
            set.connect(
                binding.textViewDocumentSize.id,
                ConstraintSet.TOP,
                binding.textViewDocumentName.id,
                ConstraintSet.BOTTOM
            )
            set.setMargin(binding.textViewDocumentSize.id, ConstraintSet.TOP, ViewUtils.dpToPx(6))
            set.clear(binding.textViewDocumentSize.id, ConstraintSet.BOTTOM)
        }
        set.applyTo(binding.constraintLayout)
        val lp = binding.imageViewDocumentIcon.layoutParams as ConstraintLayout.LayoutParams
        if (hasThumbnail) {
            lp.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            lp.height = 0
            lp.setMargins(0, ViewUtils.dpToPx(16), 0, ViewUtils.dpToPx(16))
        } else {
            lp.width = ViewUtils.dpToPx(60)
            lp.height = ViewUtils.dpToPx(70)
        }
        binding.imageViewDocumentIcon.layoutParams = lp
    }

    private fun initRVForMedias(hasThumbnails: Boolean) {
        if (documentURIs.size == 1) {
            binding.rvMedias.visibility = View.GONE
        } else {
            initMediaDisplayRecyclerView()
        }
        selectedUri = documentURIs.first()
        initMedia(selectedUri)
        if (!hasThumbnails) {
            ProgressHelper.showProgress(binding.progressBar)
            viewModel.fetchDocumentPreview(requireContext(), documentURIs)
        }
    }

    private fun initMediaDisplayRecyclerView() {
        binding.rvMedias.visibility = View.VISIBLE
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.rvMedias.layoutManager = linearLayoutManager
        imageAdapter = ImageAdapter(this)
        binding.rvMedias.adapter = imageAdapter
        imageAdapter.replace(documentURIs.mapIndexed { index, singleUriData ->
            val isSelected = index == 0
            SmallMediaViewData.Builder()
                .dynamicViewType(ITEM_DOCUMENT_SMALL)
                .singleUriData(singleUriData).isSelected(isSelected)
                .build()
        })
    }

    private fun invalidateDeleteMediaIcon(mediaFilesCount: Int) {
        binding.buttonDelete.isVisible = mediaFilesCount > 1
    }

    private fun deleteCurrentMedia() {
        documentURIs.removeAt(selectedPosition)
        if (documentURIs.size == 0) {
            binding.buttonBack.performClick()
            return
        } else {
            if (selectedPosition == documentURIs.size) {
                selectedPosition -= 1
            }
            val updatedMedias = documentURIs.mapIndexed { index, singleMediaUri ->
                val isSelected = index == selectedPosition
                val smallMediaViewData = SmallMediaViewData.Builder()
                    .dynamicViewType(ITEM_DOCUMENT_SMALL)
                    .singleUriData(singleMediaUri)
                    .isSelected(isSelected).build()
                if (isSelected) {
                    selectedUri = smallMediaViewData.singleUriData
                }
                smallMediaViewData
            }
            imageAdapter.replace(updatedMedias)
            binding.rvMedias.isVisible = updatedMedias.size > 1
            initMedia(selectedUri)
        }
    }

    override fun mediaSelected(position: Int, smallMediaViewData: SmallMediaViewData) {
        if (selectedPosition == position) return
        showUpdatedPositionData(position, smallMediaViewData)
    }

    private fun showUpdatedPositionData(position: Int, viewData: SmallMediaViewData) {
        selectedPosition = position
        selectedUri = viewData.singleUriData
        initMedia(selectedUri)
        val items = imageAdapter.items()
            .filterIsInstance(SmallMediaViewData::class.java)
            .mapIndexed { index, item ->
                item.toBuilder().isSelected(position == index).build()
            }
        imageAdapter.replace(items)
    }

    private fun initSendClick() {
        val text = binding.etConversation.text.trim().toString()
        val intent = Intent()
        intent.putExtra(
            BUNDLE_MEDIA_EXTRAS,
            mediaExtras.toBuilder().mediaUris(documentURIs).conversation(text).build()
        )
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    //result callback for new document pick
    private val browsePickerLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result?.resultCode == Activity.RESULT_OK) {
                val uris = MediaUtils.getExternalIntentPickerUris(result.data)
                viewModel.fetchUriDetails(requireContext(), uris) {
                    val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                        requireContext(), it
                    )
                    updateMediaUris(mediaUris, saveInCache = true)
                }
            }
        }

    private fun updateMediaUris(
        mediaUris: ArrayList<SingleUriData>,
        saveInCache: Boolean = false,
    ) {
        if (mediaUris.isNotEmpty()) {
            if (documentURIs.size == 1) {
                initMediaDisplayRecyclerView()
            }
            val uris = if (saveInCache) {
                AndroidUtil.moveAttachmentToCache(
                    requireContext(),
                    *mediaUris.toTypedArray()
                )

            } else {
                mediaUris
            }
            documentURIs.addAll(uris)
            imageAdapter.replace(documentURIs.mapIndexed { index, singleMediaUri ->
                val isSelected = index == selectedPosition
                SmallMediaViewData.Builder()
                    .dynamicViewType(ITEM_DOCUMENT_SMALL)
                    .singleUriData(singleMediaUri)
                    .isSelected(isSelected)
                    .build()
            })
            invalidateDeleteMediaIcon(documentURIs.size)
            ProgressHelper.showProgress(binding.progressBar)
            viewModel.fetchDocumentPreview(requireContext(), uris)
        }
    }
}