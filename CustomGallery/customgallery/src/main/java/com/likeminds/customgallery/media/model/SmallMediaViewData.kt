package com.likeminds.customgallery.media.model;


import android.os.Parcelable
import com.likeminds.customgallery.utils.model.BaseViewType
import com.likeminds.customgallery.utils.model.ITEM_MEDIA_SMALL
import kotlinx.parcelize.Parcelize

@Parcelize
class SmallMediaViewData private constructor(
    val singleUriData: SingleUriData,
    val isSelected: Boolean,
    val dynamicViewType: Int?
) : BaseViewType, Parcelable {
    override val viewType: Int
        get() = dynamicViewType ?: ITEM_MEDIA_SMALL

    class Builder {
        private var singleUriData: SingleUriData = SingleUriData.Builder().build()
        private var isSelected: Boolean = false
        private var dynamicViewType: Int? = null

        fun singleUriData(singleUriData: SingleUriData) =
            apply { this.singleUriData = singleUriData }

        fun isSelected(isSelected: Boolean) = apply { this.isSelected = isSelected }
        fun dynamicViewType(dynamicViewType: Int?) =
            apply { this.dynamicViewType = dynamicViewType }

        fun build() = SmallMediaViewData(
            singleUriData,
            isSelected,
            dynamicViewType
        )
    }

    fun toBuilder(): Builder {
        return Builder().singleUriData(singleUriData)
            .isSelected(isSelected)
            .dynamicViewType(dynamicViewType)
    }
}
