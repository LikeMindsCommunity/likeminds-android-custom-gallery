package com.likeminds.customgallery.media.model

import android.os.Parcelable
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_SMALL_ADD_MEDIA
import kotlinx.parcelize.Parcelize

@Parcelize
class SmallAddMediaViewData private constructor(
    val fileType: String
) : BaseViewType, Parcelable {
    override val viewType: Int
        get() = ITEM_SMALL_ADD_MEDIA

    class Builder {
        private var fileType: String = ""

        fun fileType(fileType: String) = apply { this.fileType = fileType }

        fun build() = SmallAddMediaViewData(fileType)
    }

    fun toBuilder(): Builder {
        return Builder().fileType(fileType)
    }
}
