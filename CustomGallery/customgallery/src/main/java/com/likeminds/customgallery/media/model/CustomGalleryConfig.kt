package com.likeminds.customgallery.media.model

class CustomGalleryConfig private constructor(
    val mediaTypes: List<String>,
    val isEditingEnabled: Boolean,
    val allowMultipleSelect: Boolean,
    val inputText: String
) {
    class Builder() {
        private var mediaTypes: List<String> = listOf()
        private var isEditingEnabled: Boolean = false
        private var allowMultipleSelect: Boolean = false
        private var inputText: String = ""

        fun mediaTypes(mediaTypes: List<String>) = apply { this.mediaTypes = mediaTypes }
        fun isEditingEnabled(isEditingEnabled: Boolean) =
            apply { this.isEditingEnabled = isEditingEnabled }

        fun allowMultipleSelect(allowMultipleSelect: Boolean) =
            apply { this.allowMultipleSelect = allowMultipleSelect }

        fun inputText(inputText: String) = apply { this.inputText = inputText }

        fun build() = CustomGalleryConfig(
            mediaTypes,
            isEditingEnabled,
            allowMultipleSelect,
            inputText
        )
    }

    fun toBuilder(): Builder {
        return Builder().mediaTypes(mediaTypes)
            .isEditingEnabled(isEditingEnabled)
            .allowMultipleSelect(allowMultipleSelect)
            .inputText(inputText)
    }
}