package com.likeminds.customgallery.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.likeminds.customgallery.media.model.AUDIO
import com.likeminds.customgallery.media.model.IMAGE
import com.likeminds.customgallery.media.model.PDF
import com.likeminds.customgallery.media.model.VIDEO
import com.likeminds.customgallery.utils.file.util.FileUtil
import com.likeminds.customgallery.utils.file.util.isLargeFile
import java.io.File

object ValueUtils {
    @JvmStatic
    fun <K, V> getOrDefault(map: Map<K, V>, key: K, defaultValue: V): V? {
        return if (map.containsKey(key)) map[key] else defaultValue
    }

    fun String?.getMediaType(): String? {
        var mediaType: String? = null
        if (this != null) {
            when {
                this.startsWith("image") -> mediaType = IMAGE
                this.startsWith("video") -> mediaType = VIDEO
                this == "application/pdf" -> mediaType = PDF
                this.startsWith("audio") -> mediaType = AUDIO
            }
        }
        return mediaType
    }

    fun Uri?.getMediaType(context: Context): String? {
        var mediaType: String? = null
        this?.let {
            mediaType = this.getMimeType(context).getMediaType()
        }
        return mediaType
    }

    fun Uri.getMimeType(context: Context): String? {
        var type = context.contentResolver.getType(this)
        if (type == null) {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(this.toString())
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
        return type
    }

    fun Uri?.isValidSize(context: Context): Boolean {
        if (this == null) {
            return false
        }
        val path = FileUtil.getRealPath(context, this).path
        if (path.isNotEmpty()) {
            return !File(path).isLargeFile
        }
        return false
    }

    /**
     * This function run filter and map operation in single loop
     */
    inline fun <T, R, P> Iterable<T>.filterThenMap(
        predicate: (T) -> Pair<Boolean, P>,
        transform: (Pair<T, P>) -> R
    ): List<R> {
        return filterThenMap(ArrayList(), predicate, transform)
    }

    inline fun <T, R, P, C : MutableCollection<in R>>
            Iterable<T>.filterThenMap(
        collection: C, predicate: (T) -> Pair<Boolean, P>,
        transform: (Pair<T, P>) -> R
    ): C {
        for (element in this) {
            val response = predicate(element)
            if (response.first) {
                collection.add(transform(Pair(element, response.second)))
            }
        }
        return collection
    }
}