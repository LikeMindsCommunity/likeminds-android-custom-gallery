package com.likeminds.customgallery.media.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.collabmates.sdk.media.model.*
import com.collabmates.sdk.utils.filterThenMap
import com.giphy.sdk.core.models.Media
import com.likeminds.customgallery.media.MediaRepository
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.utils.GiphyUtil
import com.likeminds.customgallery.utils.coroutine.*
import com.likeminds.customgallery.utils.file.util.FileUtil
import com.likeminds.customgallery.utils.model.BaseViewType

internal class MediaViewModel constructor(
    private val mediaRepository: MediaRepository,
) : ViewModel() {
    private val localFolders by lazy { MutableLiveData<List<MediaFolderViewData>>() }
    private val bucketMedias by lazy { MutableLiveData<List<BaseViewType>>() }

    private val localDocumentFiles by lazy { MutableLiveData<List<BaseViewType>>() }
    private val documentMediaList by lazy { ArrayList<MediaViewData>() }
    private val documentPreviewLiveData by lazy { MutableLiveData<List<SingleUriData>>() }
    val updatedUriDataList by lazy { MutableLiveData<List<SingleUriData>>() }

    private val localAudioFileLists by lazy { MutableLiveData<List<BaseViewType>>() }
    private val audioMediaList by lazy { ArrayList<MediaViewData>() }
    val mediaListUri = MutableLiveData<List<SingleUriData>>()
    val audioByteArray = MutableLiveData<ByteArray>()

    private val getMediaBrowserViewData by lazy { MediaBrowserViewData() }

    private val giphyMedia = MutableLiveData<Pair<Boolean, Uri?>>()

    fun getGiphyMedia(): LiveData<Pair<Boolean, Uri?>> {
        return giphyMedia
    }

    fun getDocumentPreview(): LiveData<List<SingleUriData>> {
        return documentPreviewLiveData
    }

    private fun onError(e: Throwable) {
        Log.e("MediaViewModel", "fetch - ", e)
    }

    /**
     * Fetches document preview asynchronously
     */
    fun fetchDocumentPreview(
        context: Context, uris: List<SingleUriData>,
    ) = viewModelScope.launchDefault {
        val updatedUris = uris.filter { singleUriData ->
            singleUriData.thumbnailUri == null
        }.mapNotNull { singleUriData ->
            val uri = MediaUtils.getDocumentPreview(context, singleUriData.uri)
            if (uri != null) {
                singleUriData.toBuilder().thumbnailUri(uri).build()
            } else {
                null
            }
        }
        documentPreviewLiveData.postValue(updatedUris)
    }

    fun fetchExternallySharedUriData(context: Context, uris: List<Uri>) =
        viewModelScope.launchDefault {
            val dataList = uris.mapNotNull { uri ->
                val singleUriData = mediaRepository.getExternallySharedUriDetail(context, uri)
                    ?: return@mapNotNull null
                return@mapNotNull when (singleUriData.fileType) {
                    IMAGE -> {
                        val newUri =
                            FileUtil.getSharedImageUri(context, uri) ?: return@mapNotNull null
                        singleUriData.toBuilder().uri(newUri).build()
                    }
                    GIF -> {
                        val newUri =
                            FileUtil.getSharedGifUri(context, uri) ?: return@mapNotNull null
                        singleUriData.toBuilder().uri(newUri).build()
                    }
                    VIDEO -> {
                        val newUri =
                            FileUtil.getSharedVideoUri(context, uri) ?: return@mapNotNull null
                        val thumbnailUri = FileUtil.getVideoThumbnailUri(context, uri)
                        singleUriData.toBuilder()
                            .uri(newUri)
                            .thumbnailUri(thumbnailUri)
                            .build()
                    }
                    PDF -> {
                        val newUri =
                            FileUtil.getSharedPdfUri(context, uri) ?: return@mapNotNull null
                        val thumbnailUri = MediaUtils.getDocumentPreview(context, uri)
                        singleUriData.toBuilder()
                            .uri(newUri)
                            .thumbnailUri(thumbnailUri)
                            .build()
                    }
                    AUDIO -> {
                        val newUri =
                            FileUtil.getSharedAudioUri(context, uri) ?: return@mapNotNull null
                        val thumbnailUri = FileUtil.getAudioThumbnail(context, uri)
                        singleUriData.toBuilder()
                            .uri(newUri)
                            .thumbnailUri(thumbnailUri)
                            .build()
                    }
                    else -> null
                }
            }
            updatedUriDataList.postValue(dataList)
        }

    fun fetchAllFolders(
        context: Context,
        mediaTypes: List<String>,
    ): LiveData<List<MediaFolderViewData>> {
        mediaRepository.getLocalFolders(context, mediaTypes, localFolders::postValue)
        return localFolders
    }

    fun fetchMediaInBucket(
        context: Context,
        bucketId: String,
        mediaTypes: MutableList<String>,
    ): LiveData<List<BaseViewType>> {
        mediaRepository.getMediaInBucket(context, bucketId, mediaTypes) { medias ->
            val mediaList = ArrayList<BaseViewType>()
            var headerName = ""
            medias.forEach { media ->
                if (media.dateTimeStampHeader != headerName) {
                    mediaList.add(getMediaHeader(media.dateTimeStampHeader()))
                    headerName = media.dateTimeStampHeader()
                }
                mediaList.add(media)
            }
            bucketMedias.postValue(mediaList)
        }
        return bucketMedias
    }

    fun fetchAllDocuments(context: Context): LiveData<List<BaseViewType>> {
        mediaRepository.getLocalDocumentFiles(context) { medias ->
            // Update documents list to be used for various purpose like sorting
            documentMediaList.clear()
            documentMediaList.addAll(medias)

            sortDocumentsByName()
        }
        return localDocumentFiles
    }

    fun fetchUriDetail(context: Context, uri: Uri, callback: (media: MediaViewData?) -> Unit) {
        mediaRepository.getLocalUriDetail(context, uri, callback)
    }

    fun fetchUriDetails(
        context: Context,
        uris: List<Uri>,
        callback: (media: List<MediaViewData>) -> Unit,
    ) {
        mediaRepository.getLocalUrisDetails(context, uris, callback)
    }

    fun sortDocumentsByName() {
        documentMediaList.sortBy { it.mediaName() }
        postDocumentListForView(documentMediaList)
    }

    fun sortDocumentsByDate() {
        documentMediaList.sortByDescending { it.date() }
        postDocumentListForView(documentMediaList)
    }

    fun filterDocumentsByKeyword(keyword: String) {
        val keywordList = keyword.split(" ")
        val updatedList = documentMediaList.filterThenMap({ media ->
            val matchedKeywords = keywordList.filter {
                media.mediaName()?.contains(it) == true
            }
            Pair(matchedKeywords.isNotEmpty(), matchedKeywords)
        }, {
            it.first.toBuilder().filteredKeywords(it.second).build()
        })

        postDocumentListForView(updatedList)
    }

    fun clearDocumentFilter() {
        postDocumentListForView(documentMediaList)
    }

    private fun postDocumentListForView(updatedList: List<MediaViewData>) {
        val mediaList = ArrayList<BaseViewType>()
        mediaList.add(getMediaBrowserViewData)
        mediaList.addAll(updatedList)
        localDocumentFiles.postValue(mediaList)
    }

    fun fetchAllAudioFiles(context: Context): LiveData<List<BaseViewType>> {
        mediaRepository.getLocalAudioFiles(context) { medias ->
            audioMediaList.clear()
            audioMediaList.addAll(medias)

            postAudioListForView(audioMediaList)
        }
        return localAudioFileLists
    }

    private fun postAudioListForView(audioMediaList: List<MediaViewData>) {
        val mediaList = ArrayList<BaseViewType>()
        mediaList.addAll(audioMediaList)
        localAudioFileLists.value = mediaList
    }

    fun filterAudioByKeyword(keyword: String) {
        val keywordList = keyword.split(" ")
        val updatedList = audioMediaList.filterThenMap({ media ->
            val matchedKeywords = keywordList.filter {
                media.mediaName()?.contains(it, true) == true
            }
            Pair(matchedKeywords.isNotEmpty(), matchedKeywords)
        }, {
            it.first.toBuilder().filteredKeywords(it.second).build()
        })

        postAudioListForView(updatedList)
    }

    fun clearAudioFilter() {
        postAudioListForView(audioMediaList)
    }

    fun createThumbnailForAudio(
        context: Context,
        mediaUris: MutableList<SingleUriData>?,
    ) {
        viewModelScope.launchDefault {
            mediaListUri.postValue(mediaRepository.createThumbnailForAudio(context, mediaUris))
        }
    }

    private fun getMediaHeader(title: String): MediaHeaderViewData {
        return MediaHeaderViewData.Builder().title(title).build()
    }

    fun convertUriToByteArray(context: Context, uri: Uri) {
        viewModelScope.launchDefault {
            audioByteArray.postValue(mediaRepository.convertUriToByteArray(context, uri))
        }
    }

    fun getGiphyUri(context: Context, media: Media) {
        giphyMedia.postValue(Pair(true, null))
        GiphyUtil.getGifLink(media)?.let { link ->
            viewModelScope.launchIO {
                FileUtil.getGifUri(context, link)?.let { uri ->
                    giphyMedia.postValue(Pair(false, uri))
                }
            }
        }
    }
}