package com.likeminds.customgallery.utils.file.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.util.Size
import androidx.core.content.FileProvider
import com.likeminds.customgallery.utils.file.model.FileData
import com.likeminds.customgallery.utils.file.util.Constants.FileConstants.CLOUD_FILE
import com.likeminds.customgallery.utils.file.util.Constants.FileConstants.LOCAL_PROVIDER
import com.likeminds.customgallery.utils.file.util.Constants.FileConstants.UNKNOWN_FILE_CHOOSER
import com.likeminds.customgallery.utils.file.util.Constants.FileConstants.UNKNOWN_PROVIDER
import com.likeminds.customgallery.utils.file.util.Paths.isCloudFile
import com.likeminds.customgallery.utils.file.util.Paths.isUnknownProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    private const val TAG = "FileUtil"

    /**
     * Returns sd card path for an Uri
     * @param uri single Uri
     */
    fun getRealPath(context: Context, uri: Uri): FileData {
        val contentResolver = context.contentResolver
        val pathTempFile = getFullPathTemp(context, uri)
        val file = File(pathTempFile)
        val returnedPath = PathUtil.getPath(context, uri)
        return when {
            //Cloud
            uri.isCloudFile -> {
                downloadFile(contentResolver, file, uri)
                FileData(CLOUD_FILE, pathTempFile)
            }
            //Third Party App
            returnedPath.isBlank() -> {
                downloadFile(contentResolver, file, uri)
                FileData(UNKNOWN_FILE_CHOOSER, pathTempFile)
            }
            //Unknown Provider or unknown mime type
            uri.isUnknownProvider(returnedPath, contentResolver) -> {
                downloadFile(contentResolver, file, uri)
                FileData(UNKNOWN_PROVIDER, pathTempFile)
            }
            //LocalFile
            else -> {
                FileData(LOCAL_PROVIDER, returnedPath)
            }
        }
    }

    private fun getFullPathTemp(context: Context, uri: Uri): String {
        val folder: File? = context.getExternalFilesDir("Temp")
        return "${folder.toString()}/${getFileName(context, uri)}"
    }

    private fun getFileName(context: Context?, fileUri: Uri): String? {
        var fileName: String? = null
        if (fileUri.scheme == ContentResolver.SCHEME_CONTENT) {
            context?.contentResolver?.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }
        } else if (fileUri.scheme == ContentResolver.SCHEME_FILE) {
            fileName = File(fileUri.path.toString()).name
        } else {
            fileName = fileUri.path
            val cut = fileName?.lastIndexOf('/') ?: -1
            if (cut != -1) fileName = fileName?.substring(cut.plus(1))
        }
        return fileName
    }

    /**
     *  Method that downloads the file to an internal folder at the root of the project.
     *  For cases where the file has an unknown provider, cloud files and for users using
     *  third-party file explorer api.
     *
     * @param uri of the file
     * @return new path string
     */
    fun downloadFile(
        contentResolver: ContentResolver,
        file: File,
        uri: Uri
    ): Boolean {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int = input.read(buffer)
                    while (read != -1) {
                        output.write(buffer, 0, read)
                        read = input.read(buffer)
                    }
                }
            }
        } catch (e: Exception) {
            file.deleteRecursively()
            e.printStackTrace()
            Log.e(TAG, "downloadFile", e)
        }
        return true
    }

    fun compressFile(applicationContext: Context, filePath: String): File? {
        try {
            val oldExifOrientation =
                ExifInterface(filePath).getAttribute(ExifInterface.TAG_ORIENTATION)
            val bitmap = BitmapFactory.decodeFile(filePath) ?: return null
            val imagesFolder = File(applicationContext.cacheDir, "images")
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            stream.flush()
            stream.close()
            // Update the old image orientation attributes to the compressed one
            if (oldExifOrientation != null) {
                val newExif = ExifInterface(file.absolutePath)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, oldExifOrientation)
                newExif.saveAttributes()
            }
            return file
        } catch (e: IOException) {
            Log.e(
                TAG,
                "IOException while trying to compress file: " + e.localizedMessage
            )
            return null
        }
    }

    @JvmStatic
    fun getUriFromBitmapWithRandomName(
        context: Context,
        bitmap: Bitmap?,
        shareUriExternally: Boolean = false,
        isPNGFormat: Boolean = false
    ): Uri? {
        if (bitmap == null) {
            return null
        }
        val imagesFolder = File(context.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "${System.currentTimeMillis()}.png")

            val stream = FileOutputStream(file)
            val compressFormat =
                if (isPNGFormat) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            bitmap.compress(compressFormat, 100, stream)
            stream.flush()
            stream.close()
            uri = if (!shareUriExternally) {
                Uri.fromFile(file)
            } else {
                try {
                    FileProvider.getUriForFile(
                        context,
                        getFileProviderPackage(context),
                        file
                    )
                } catch (e: Exception) {
                    Log.e("LikeMinds", "provider not found, ${e.localizedMessage}")
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(
                TAG,
                "IOException while trying to write file for sharing: " + e.localizedMessage
            )
        }
        return uri
    }

    /**
     * returns the package of file provider, required for attachments
     **/
    private fun getFileProviderPackage(context: Context): String {
        return context.applicationContext.packageName
    }

    fun getSharedImageUri(context: Context, uri: Uri?): Uri? {
        if (uri == null) {
            return null
        }
        return try {
            val oldExifOrientation = ExifInterface(getRealPath(context, uri).path)
                .getAttribute(ExifInterface.TAG_ORIENTATION)
            val bitmap = getBitmapFromUri(uri, context) ?: return null
            val newUri = getUriFromBitmapWithRandomName(context, bitmap) ?: return null
            if (oldExifOrientation != null) {
                val newExif = ExifInterface(getRealPath(context, newUri).path)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, oldExifOrientation)
                newExif.saveAttributes()
            }
            newUri
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getSharedImageUri", e)
            null
        }
    }

    private fun getBitmapFromUri(uri: Uri?, context: Context): Bitmap? {
        var bitmap: Bitmap? = null
        uri?.let {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")!!
                val fileDescriptor = parcelFileDescriptor.fileDescriptor
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
            } catch (e: IOException) {
                Log.e(
                    "FileUtils",
                    "IOException while trying to get bitmap from uri: " + e.localizedMessage
                )
            }
        }
        return bitmap
    }

    fun getImageDimensions(context: Context, uri: Uri): Pair<Int, Int> {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")!!
            val fileDescriptor = parcelFileDescriptor.fileDescriptor
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
            parcelFileDescriptor.close()
            Pair(options.outWidth, options.outHeight)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Pair(0, 0)
        }
    }

    fun getVideoDimensions(context: Context, videoUri: Uri?): Pair<Int, Int> {
        val mediaMetadataRetriever: MediaMetadataRetriever
        return try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, videoUri)
            val width =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toInt() ?: -1
            val height =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toInt() ?: -1
            Pair(width, height)
        } catch (e: Exception) {
            Log.e("SDK", "error: ${e.localizedMessage}")
            Pair(-1, -1)
        }
    }

    fun getVideoThumbnailUri(context: Context, videoUri: Uri?): Uri? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoUri.toString(), HashMap())
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }
        if (bitmap == null && videoUri != null) {
            val path = getRealPath(context, videoUri).path
            if (path.isEmpty()) {
                return null
            }
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(
                    File(path), Size(600, 600), CancellationSignal()
                )
            } else {
                ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
            }
        }
        return getUriFromBitmapWithRandomName(context, bitmap)
    }

    /**
     * Returns subfolder from the main folder to the file location or empty string
     * EXAMPLE:
     * Input uriString = "content://com.android.providers.downloads.documents/document/raw%3%2Fstorage%2Femulated%2F0%2FDownload%2FsubFolder%2FsubFolder2%2Ffile.jpg"
     * Input folderRoot = "Download"
     * Output: subFolder/subFolder2/
     *
     * @param uriString Path file
     * @param folderRoot It is usually "Download"
     */
    fun getSubFolders(uriString: String, folderRoot: String = Constants.PathUri.FOLDER_DOWNLOAD) =
        uriString
            .replace("%2F", "/")
            .replace("%20", " ")
            .replace("%3A", ":")
            .split("/")
            .run {
                val indexRoot = indexOf(folderRoot)
                if (folderRoot.isNotBlank().and(indexRoot != -1)) {
                    subList(indexRoot + 1, lastIndex)
                        .joinToString(separator = "") { "$it/" }
                } else {
                    ""
                }
            }

    fun getSharedPdfUri(context: Context, oldUri: Uri?): Uri? {
        if (oldUri == null) {
            return null
        }
        var newUri: Uri? = null
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(oldUri, "r")!!
            val fileDescriptor = parcelFileDescriptor.fileDescriptor

            val pdfsFolder = File(context.cacheDir, "pdfs")
            pdfsFolder.mkdirs()
            val file = File(pdfsFolder, "${System.currentTimeMillis()}.pdf")

            val inputStream: InputStream = FileInputStream(fileDescriptor)
            val outputStream = FileOutputStream(file)

            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) {
                outputStream.write(buf, 0, len)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            newUri = try {
                FileProvider.getUriForFile(
                    context,
                    getFileProviderPackage(context),
                    file
                )
            } catch (e: Exception) {
                Log.e("LikeMinds", "provider not found, ${e.localizedMessage}")
                null
            }
        } catch (e: IOException) {
            Log.e(
                "FileUtils",
                "IOException while trying to copy pdf from uri: " + e.localizedMessage
            )
        }
        return newUri
    }

    fun getSharedVideoUri(context: Context, oldUri: Uri?): Uri? {
        var newUri: Uri? = null
        oldUri?.let {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(oldUri, "r")!!
                val fileDescriptor = parcelFileDescriptor.fileDescriptor

                val videosFolder = File(context.cacheDir, "videos")
                videosFolder.mkdirs()
                val file = File(videosFolder, "${System.currentTimeMillis()}.mp4")

                val inputStream: InputStream = FileInputStream(fileDescriptor)
                val outputStream = FileOutputStream(file)

                // Transfer bytes from in to out
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                newUri = Uri.fromFile(file)
            } catch (e: IOException) {
                Log.e(
                    "FileUtils",
                    "IOException while trying to copy video from uri: " + e.localizedMessage
                )
            }
        }
        return newUri
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    fun getAudioThumbnail(context: Context, audioUri: Uri?): Uri? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        val bfo = BitmapFactory.Options()
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, audioUri)
            val rawArt = mediaMetadataRetriever.embeddedPicture
            bitmap = if (rawArt != null) {
                BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }

        return getUriFromBitmapWithRandomName(context, bitmap)
    }

    fun getSharedAudioUri(context: Context, oldUri: Uri?): Uri? {
        var newUri: Uri? = null
        oldUri?.let {
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(oldUri, "r")!!
                val fileDescriptor = parcelFileDescriptor.fileDescriptor

                val audioFolder = File(context.cacheDir, "audios")
                audioFolder.mkdir()

                val file = File(audioFolder, "${System.currentTimeMillis()}.mp3")

                val inputStream: InputStream = FileInputStream(fileDescriptor)
                val outputStream = FileOutputStream(file)

                val buf = ByteArray(1024)
                var len: Int

                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                newUri = Uri.fromFile(file)
            } catch (e: IOException) {
                Log.e(
                    "FileUtils",
                    "IOException while trying to copy audio from uri: " + e.localizedMessage
                )
            }
        }

        return newUri
    }

    @Throws(IOException::class)
    fun createVideoFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "VID_${timeStamp}_", /* prefix */
            ".mp4", /* suffix */
            storageDir /* directory */
        )
    }

    fun getFileExtensionFromFileName(
        fileName: String?
    ): String? {
        return fileName?.substringAfterLast(".", "")
    }
}

private const val LARGE_FILE_SIZE = 100 //in MegaBytes
private const val SMALL_FILE_SIZE = 100 //in KiloBytes

val File.size get() = if (!exists()) 0.0 else length().toDouble()
private val File.sizeInKb get() = size / 1000
private val File.sizeInMb get() = sizeInKb / 1000
val File.isLargeFile get() = sizeInMb > LARGE_FILE_SIZE

/**
 * Size value should be in bytes
 * */
private val Long.sizeInKb get() = this / 1000
private val Long.sizeInMb get() = sizeInKb / 1000
val Long.isLargeFile get() = sizeInMb > LARGE_FILE_SIZE
val Long.isSmallFile get() = sizeInKb < SMALL_FILE_SIZE