package com.likeminds.customgallery.media.model

import android.os.Parcelable
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_PICKER_HEADER
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaHeaderViewData private constructor(
    var title: String,
) : Parcelable, BaseViewType {
    override val viewType: Int
        get() = ITEM_MEDIA_PICKER_HEADER

    internal class Builder {
        private var title: String = ""

        fun title(title: String) = apply { this.title = title }

        fun build() = MediaHeaderViewData(title)
    }

    fun toBuilder(): Builder {
        return Builder().title(title)
    }
}