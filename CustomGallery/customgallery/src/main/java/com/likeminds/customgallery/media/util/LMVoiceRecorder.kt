package com.likeminds.customgallery.media.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import com.collabmates.fileutil.size
import com.likeminds.customgallery.media.model.SingleUriData
import com.likeminds.customgallery.media.model.VOICE_NOTE
import java.io.File

internal class LMVoiceRecorder {

    private var mediaRecorder: MediaRecorder? = null
    private var filePath: String = ""
    private var elapsedMillis = 0L

    companion object {
        private const val THRESHOLD = 300L
    }

    fun isRecording() = mediaRecorder != null

    private fun initializeMediaRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }
    }

    fun startRecording(filePath: String) {
        initializeMediaRecorder()
        this.filePath = filePath
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        mediaRecorder!!.setOutputFile(filePath)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
        elapsedMillis = System.currentTimeMillis()
    }

    /**
     * Returns the single uri data of the recorded file for converting it to SingleUriData and posting
     * */
    fun stopRecording(context: Context): SingleUriData? {
        if (System.currentTimeMillis() - elapsedMillis < THRESHOLD) {
            mediaRecorder?.release()
            mediaRecorder = null
            elapsedMillis = 0L
            return null
        }
        elapsedMillis = 0L
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        return convertFileNameToSingleUriData(context, filePath)
    }

    fun stopRecording() {
        if (System.currentTimeMillis() - elapsedMillis < THRESHOLD) {
            mediaRecorder?.stop()
        }
        elapsedMillis = 0L
        mediaRecorder?.release()
        mediaRecorder = null
    }


    private fun convertFileNameToSingleUriData(context: Context, filePath: String): SingleUriData? {
        val uri = Uri.fromFile(File(filePath)) ?: return null
        val duration = getDuration(context, uri)
        val size = File(filePath).size
        val name = File(filePath).name
        return if (duration != null) {
            SingleUriData.builder()
                .uri(uri)
                .fileType(VOICE_NOTE)
                .size(size.toLong())
                .mediaName(name)
                .duration(duration)
                .build()
        } else {
            null
        }
    }

    private fun getDuration(context: Context, uri: Uri): Int? {
        try {
            val retriever = MediaMetadataRetriever()
            val fd = context.contentResolver.openFileDescriptor(uri, "r")?.fileDescriptor
                ?: return null
            retriever.setDataSource(fd)
            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toInt() ?: return null
            retriever.release()
            return duration / 1000
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}