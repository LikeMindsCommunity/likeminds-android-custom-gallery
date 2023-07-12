package com.likeminds.customgallery.media.model

import android.os.Parcelable
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_BROWSE
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaBrowserViewData : Parcelable, BaseViewType {
    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_BROWSE
}