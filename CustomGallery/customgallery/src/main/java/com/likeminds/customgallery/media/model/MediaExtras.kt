package com.likeminds.customgallery.media.model

import android.os.Parcelable
import com.giphy.sdk.core.models.Media
import com.likeminds.customgallery.chatroom.detail.model.CollabcardAttachmentViewData
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaExtras private constructor(
    var isExternallyShared: Boolean,
    var mediaScreenType: Int,
    var title: String?,
    var subtitle: String?,
    var attachments: List<CollabcardAttachmentViewData>?,
    var singleUriData: SingleUriData?,
    var mediaUris: ArrayList<SingleUriData>?,
    var selectedMediaPosition: Int?,
    var conversation: String?,
    var text: String?,
    var position: Int?,
    var medias: List<MediaSwipeViewData>?,
    var chatroomId: String?,
    var chatroomName: String?,
    var communityId: Int?,
    var cropSquare: Boolean?,
    var downloadableContentTypes: List<String>?,
    var chatroomType: String?,
    var communityName: String?,
    var searchKey: String?,
    var giphyMedia: Media?,
    var conversationId: String?,
    var isSecretChatroom: Boolean?
) : Parcelable {

    internal class Builder {
        private var isExternallyShared: Boolean = false
        private var mediaScreenType: Int = -1
        private var title: String? = null
        private var subtitle: String? = null
        private var attachments: List<CollabcardAttachmentViewData>? = null
        private var singleUriData: SingleUriData? = null
        private var mediaUris: ArrayList<SingleUriData>? = null
        private var selectedMediaPosition: Int? = null
        private var conversation: String? = null
        private var text: String? = null
        private var position: Int? = null
        private var medias: List<MediaSwipeViewData>? = null
        private var chatroomId: String? = null
        private var chatroomName: String? = null
        private var communityId: Int? = null
        private var cropSquare: Boolean? = null
        private var downloadableContentTypes: List<String>? = null
        private var chatroomType: String? = null
        private var communityName: String? = null
        private var searchKey: String? = null
        private var giphyMedia: Media? = null
        private var conversationId: String? = null
        private var isSecretChatroom: Boolean? = null

        fun isExternallyShared(isExternallyShared: Boolean) =
            apply { this.isExternallyShared = isExternallyShared }

        fun mediaScreenType(mediaScreenType: Int) = apply { this.mediaScreenType = mediaScreenType }
        fun title(title: String?) = apply { this.title = title }
        fun subtitle(subtitle: String?) = apply { this.subtitle = subtitle }
        fun attachments(attachments: List<CollabcardAttachmentViewData>?) =
            apply { this.attachments = attachments }

        fun singleUriData(singleUriData: SingleUriData?) =
            apply { this.singleUriData = singleUriData }

        fun mediaUris(mediaUris: ArrayList<SingleUriData>?) = apply { this.mediaUris = mediaUris }
        fun selectedMediaPosition(selectedMediaPosition: Int?) =
            apply { this.selectedMediaPosition = selectedMediaPosition }

        fun conversation(conversation: String?) = apply { this.conversation = conversation }
        fun text(text: String?) = apply { this.text = text }
        fun position(position: Int?) = apply { this.position = position }
        fun medias(medias: List<MediaSwipeViewData>?) = apply { this.medias = medias }
        fun chatroomId(chatroomId: String?) = apply { this.chatroomId = chatroomId }
        fun chatroomName(chatroomName: String?) = apply { this.chatroomName = chatroomName }
        fun communityId(communityId: Int?) = apply { this.communityId = communityId }
        fun cropSquare(cropSquare: Boolean?) = apply { this.cropSquare = cropSquare }
        fun downloadableContentTypes(downloadableContentTypes: List<String>?) =
            apply { this.downloadableContentTypes = downloadableContentTypes }

        fun chatroomType(chatroomType: String?) = apply { this.chatroomType = chatroomType }
        fun communityName(communityName: String?) = apply { this.communityName = communityName }
        fun searchKey(searchKey: String?) = apply { this.searchKey = searchKey }
        fun giphyMedia(giphyMedia: Media?) = apply { this.giphyMedia = giphyMedia }
        fun conversationId(conversationId: String?) = apply { this.conversationId = conversationId }
        fun isSecretChatroom(isSecretChatroom: Boolean?) =
            apply { this.isSecretChatroom = isSecretChatroom }

        fun build() = MediaExtras(
            isExternallyShared,
            mediaScreenType,
            title,
            subtitle,
            attachments,
            singleUriData,
            mediaUris,
            selectedMediaPosition,
            conversation,
            text,
            position,
            medias,
            chatroomId,
            chatroomName,
            communityId,
            cropSquare,
            downloadableContentTypes,
            chatroomType,
            communityName,
            searchKey,
            giphyMedia,
            conversationId,
            isSecretChatroom
        )
    }

    fun toBuilder(): Builder {
        return Builder().isExternallyShared(isExternallyShared)
            .mediaScreenType(mediaScreenType)
            .title(title)
            .subtitle(subtitle)
            .attachments(attachments)
            .singleUriData(singleUriData)
            .mediaUris(mediaUris)
            .selectedMediaPosition(selectedMediaPosition)
            .conversation(conversation)
            .text(text)
            .position(position)
            .medias(medias)
            .chatroomId(chatroomId)
            .chatroomName(chatroomName)
            .communityId(communityId)
            .cropSquare(cropSquare)
            .downloadableContentTypes(downloadableContentTypes)
            .chatroomType(chatroomType)
            .communityName(communityName)
            .searchKey(searchKey)
            .giphyMedia(giphyMedia)
            .conversationId(conversationId)
            .isSecretChatroom(isSecretChatroom)
    }
}