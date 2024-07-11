package com.likeminds.customgallery.media.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.likeminds.customgallery.R
import com.likeminds.customgallery.media.model.MediaViewData
import com.likeminds.customgallery.media.model.SingleUriData
import com.likeminds.customgallery.utils.ViewUtils
import com.likeminds.customgallery.utils.file.util.*
import java.io.InputStream

object MediaUtils {

    private const val TAG = "MediaUtils"

    /**
     * Fetches the pdf preview using Android's core [PdfRenderer]
     */
    fun getDocumentPreview(context: Context, uri: Uri): Uri? {
        try {
            //creates a file descriptor
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null

            //creates an object of renderer of pdf
            val renderer = PdfRenderer(pfd)

            if (renderer.pageCount >= 0) {
                //opens first page
                val page = renderer.openPage(0)

                //create bitmap for the preview
                val bitmap = Bitmap.createBitmap(
                    page.width,
                    page.height,
                    Bitmap.Config.ARGB_8888
                )

                //Make background white, if pdf is transparent
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                //render first page into the bitmap
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                //close the page
                page.close()

                //close the renderer
                renderer.close()

                //convert the bitmap into uri and return
                return FileUtil.getUriFromBitmapWithRandomName(context, bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getDocumentPreview", e)
        }
        return null
    }

    fun getTextSizes(): Array<Float> {
        return arrayOf(
            ViewUtils.spToPx(16), ViewUtils.spToPx(20), ViewUtils.spToPx(24),
            ViewUtils.spToPx(28), ViewUtils.spToPx(32), ViewUtils.spToPx(36)
        )
    }

    fun getTextTypeFaces(context: Context): Array<Typeface?> {
        val regular = ResourcesCompat.getFont(context, R.font.roboto_regular)
        val bold = ResourcesCompat.getFont(context, R.font.roboto_bold)
        val signature = ResourcesCompat.getFont(context, R.font.signature)
        val condensed = ResourcesCompat.getFont(context, R.font.roboto_condensed_bold)
        return arrayOf(regular, bold, signature, condensed)
    }

    fun getTextIcons(): Array<Int> {
        return arrayOf(
            R.drawable.ic_text, R.drawable.ic_text_bold,
            R.drawable.ic_text_signature, R.drawable.ic_text_condensed
        )
    }

    fun convertMediaViewDataToSingleUriData(
        context: Context,
        medias: List<MediaViewData>?
    ): ArrayList<SingleUriData> {
        var largeFileSelected = false
        val mediaUris = arrayListOf<SingleUriData>()

        if (!medias.isNullOrEmpty()) {
            medias.forEach { mediaViewData ->
                if (!mediaViewData.size.isLargeFile) {
                    mediaUris.add(
                        SingleUriData.Builder()
                            .mediaName(mediaViewData.mediaName)
                            .uri(mediaViewData.uri)
                            .fileType(mediaViewData.mediaType)
                            .size(mediaViewData.size)
                            .duration(mediaViewData.duration)
                            .mediaName(mediaViewData.mediaName)
                            .duration(mediaViewData.duration)
                            .pdfPageCount(mediaViewData.pdfPageCount)
                            .localFilePath(FileUtil.getRealPath(context, mediaViewData.uri).path)
                            .thumbnailUri(mediaViewData.thumbnailUri)
                            .build()
                    )
                } else {
                    largeFileSelected = true
                }
            }
        }

        if (largeFileSelected) {
            ViewUtils.showShortToast(
                context, context.getString(R.string.large_file_select_error_message)
            )
        }
        return mediaUris
    }

    fun getExternalIntentPickerUris(data: Intent?): ArrayList<Uri> {
        val uris = arrayListOf<Uri>()
        if (data != null) {
            val mediaUriCount = data.clipData?.itemCount ?: 0
            if (mediaUriCount > 0) {
                for (i in 0 until mediaUriCount) {
                    val mediaUri = data.clipData?.getItemAt(i)?.uri
                    if (mediaUri != null) uris.add(mediaUri)
                }
            } else {
                val mediaUri = data.data
                if (mediaUri != null) uris.add(mediaUri)
            }
        }
        return uris
    }

    /**
     * Return the file size in string format e.g. 10 MB
     * */
    fun getFileSizeText(sizeBytes: Long): String {
        return MemoryUnitFormat.formatBytes(sizeBytes)
    }

    /**
     * Return the size of a local media uri in bytes
     * */
    fun Uri.getMediaSize(context: Context?): Long {
        val `in`: InputStream = context?.contentResolver?.openInputStream(this) ?: return 0
        var size: Long = 0
        val buffer = ByteArray(4096)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            size += read.toLong()
        }
        `in`.close()
        return size
    }


    fun isVideoType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("video/")
    }

    fun isAudioType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("audio/")
    }

    fun isImageType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("image/")
    }

    fun isPdfType(contentType: String?): Boolean {
        return null != contentType && contentType == "application/pdf"
    }
}