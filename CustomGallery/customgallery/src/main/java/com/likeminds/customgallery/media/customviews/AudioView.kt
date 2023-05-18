package com.likeminds.customgallery.media.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.SimpleItemAnimator
import com.collabmates.sdk.LMAnalytics
import com.collabmates.sdk.auth.LoginPreferences
import com.likeminds.customgallery.chatroom.common.model.CollabcardViewData
import com.likeminds.customgallery.chatroom.create.adapter.CollabcardItemAdapter
import com.likeminds.customgallery.chatroom.detail.adapter.CollabcardDetailAdapterListener
import com.likeminds.customgallery.chatroom.detail.model.CollabcardAnswerViewData
import com.likeminds.customgallery.chatroom.detail.model.CollabcardAttachmentViewData
import com.likeminds.customgallery.databinding.LayoutAudioBinding

internal class AudioView(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), CollabcardItemAdapter.CollabcardDetailItemAdapterListener {

    var binding = LayoutAudioBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private val attachments = arrayListOf<CollabcardAttachmentViewData>()

    private var adapter: CollabcardItemAdapter? = null

    private lateinit var chatroomAdapterListener: CollabcardDetailAdapterListener
    private lateinit var loginPreferences: LoginPreferences
    private var mediaActionVisible = false
    private var mediaUploadFailed = false
    private var isProgressFocussed = false

    fun initialize(
        attachments: List<CollabcardAttachmentViewData>,
        chatroomAdapterListener: CollabcardDetailAdapterListener,
        mediaActionVisible: Boolean,
        loginPreferences: LoginPreferences,
        mediaUploadFailed: Boolean = false,
    ) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        this.loginPreferences = loginPreferences
        this.chatroomAdapterListener = chatroomAdapterListener
        this.mediaActionVisible = mediaActionVisible
        this.mediaUploadFailed = mediaUploadFailed
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = CollabcardItemAdapter(
            loginPreferences,
            collabcardDetailItemAdapterListener = this
        )
        adapter?.replace(attachments.toList())
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.recyclerView.adapter = adapter
    }

    fun replaceList(attachments: List<CollabcardAttachmentViewData>) {
        this.attachments.clear()
        this.attachments.addAll(attachments)
        adapter?.replace(attachments)
    }

    override fun onLongPressConversation(
        conversation: CollabcardAnswerViewData,
        itemPosition: Int
    ) {
        chatroomAdapterListener.onLongPressConversation(
            conversation,
            itemPosition,
            LMAnalytics.Sources.SOURCE_MESSAGE_REACTIONS_FROM_LONG_PRESS
        )
    }

    override fun onLongPressChatRoom(chatRoom: CollabcardViewData, itemPosition: Int) {
        chatroomAdapterListener.onLongPressChatRoom(chatRoom, itemPosition)
    }

    override fun isMediaActionVisible(): Boolean {
        return mediaActionVisible
    }

    override fun isMediaUploadFailed(): Boolean {
        return mediaUploadFailed
    }

    override fun onAudioConversationActionClicked(
        data: CollabcardAttachmentViewData,
        position: String,
        childPosition: Int,
        progress: Int
    ) {
        chatroomAdapterListener.onAudioConversationActionClicked(
            data,
            position,
            childPosition,
            progress
        )
    }

    override fun onAudioChatroomActionClicked(
        data: CollabcardAttachmentViewData,
        childPosition: Int,
        progress: Int
    ) {
        chatroomAdapterListener.onAudioChatroomActionClicked(data, childPosition, progress)
    }

    override fun isSelectionEnabled(): Boolean {
        return chatroomAdapterListener.isSelectionEnabled()
    }

    override fun onConversationSeekBarChanged(
        progress: Int,
        attachmentViewData: CollabcardAttachmentViewData,
        parentConversationId: String,
        childPosition: Int
    ) {
        chatroomAdapterListener.onConversationSeekbarChanged(
            progress,
            attachmentViewData,
            parentConversationId,
            childPosition
        )
    }

    override fun onChatroomSeekbarChanged(
        progress: Int,
        attachmentViewData: CollabcardAttachmentViewData,
        childPosition: Int
    ) {
        chatroomAdapterListener.onChatroomSeekbarChanged(
            progress,
            attachmentViewData,
            childPosition
        )
    }

    override fun onSeekBarFocussed(value: Boolean) {
        isProgressFocussed = value
    }

    fun isProgressFocussed(): Boolean {
        return isProgressFocussed
    }
}