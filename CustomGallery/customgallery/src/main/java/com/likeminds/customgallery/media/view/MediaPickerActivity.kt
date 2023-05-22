package com.likeminds.customgallery.media.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.NavHostFragment
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.R
import com.likeminds.customgallery.media.MediaRepository
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.ViewUtils.currentFragment
import com.likeminds.customgallery.utils.customview.BaseAppCompatActivity
import com.likeminds.customgallery.utils.permissions.Permission
import com.likeminds.customgallery.utils.permissions.PermissionDeniedCallback
import com.likeminds.customgallery.utils.permissions.PermissionManager

internal class MediaPickerActivity : BaseAppCompatActivity() {

    private lateinit var mediaPickerExtras: MediaPickerExtras

    companion object {
        const val PICK_MEDIA = 5001
        const val BROWSE_MEDIA = 5002
        const val BROWSE_DOCUMENT = 5003
        const val PICK_CAMERA = 5004
        const val CROP_IMAGE = 5005

        private const val ARG_MEDIA_PICKER_EXTRAS = "mediaPickerExtras"
        const val ARG_MEDIA_PICKER_RESULT = "mediaPickerResult"

        fun start(context: Context, extras: MediaPickerExtras) {
            val intent = Intent(context, MediaPickerActivity::class.java)
            intent.apply {
                putExtras(Bundle().apply {
                    putParcelable(ARG_MEDIA_PICKER_EXTRAS, extras)
                })
            }
            context.startActivity(intent)
        }

        fun getIntent(context: Context, extras: MediaPickerExtras): Intent {
            return Intent(context, MediaPickerActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putParcelable(ARG_MEDIA_PICKER_EXTRAS, extras)
                })
            }
        }
    }

    private val browserMediaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uris = MediaUtils.getExternalIntentPickerUris(result.data)
                val mediaRepository = MediaRepository()
                mediaRepository.getLocalUrisDetails(this, uris) {
                    val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                        this, it
                    )
                    if (mediaUris.isNotEmpty()) {
                        showPickDocumentsListScreen(
                            *mediaUris.toTypedArray(),
                            saveInCache = true
                        )
                    }
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    private val documentSendLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data =
                    result.data?.extras?.getParcelable<MediaExtras>(MediaActivity.BUNDLE_MEDIA_EXTRAS)
                        ?: return@registerForActivityResult
                val customGalleryResult = CustomGalleryResult.Builder()
                    .medias(data.mediaUris?.toList() ?: listOf())
                    .text(data.text)
                    .build()
                val intent = Intent().apply {
                    putExtras(Bundle().apply {
                        putParcelable(CustomGallery.ARG_CUSTOM_GALLERY_RESULT, customGalleryResult)
                    })
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else if (result?.resultCode == Activity.RESULT_FIRST_USER) {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_picker)
        val extras = intent.extras?.getParcelable<MediaPickerExtras>(ARG_MEDIA_PICKER_EXTRAS)
        if (extras == null) {
            throw IllegalArgumentException("Arguments are missing")
        } else {
            mediaPickerExtras = extras
        }

        checkStoragePermission()
    }


    private fun checkStoragePermission() {
        PermissionManager.performTaskWithPermission(
            this,
            { startMediaPickerFragment() },
            Permission.getStoragePermissionData(),
            showInitialPopup = true,
            showDeniedPopup = true,
            permissionDeniedCallback = object : PermissionDeniedCallback {
                override fun onDeny() {
                    onBackPressed()
                }

                override fun onCancel() {
                    onBackPressed()
                }
            }
        )
    }

    private fun startMediaPickerFragment() {
        val systemDocumentInitiated = checkIfDocumentPickerInitiated()

        if (!systemDocumentInitiated) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host) as? NavHostFragment ?: return
            val graphInflater = navHostFragment.navController.navInflater
            val navGraph = graphInflater.inflate(R.navigation.nav_media_picker_graph)
            val navController = navHostFragment.navController

            when {
                MediaType.isImageOrVideo(mediaPickerExtras.mediaTypes) -> {
                    navGraph.setStartDestination(R.id.mediaPickerFolderFragment)
                }
                MediaType.isPDF(mediaPickerExtras.mediaTypes) -> {
                    navGraph.setStartDestination(R.id.mediaPickerDocumentFragment)
                }
                MediaType.isAudio(mediaPickerExtras.mediaTypes) -> {
                    navGraph.setStartDestination(R.id.mediaPickerAudioFragment)
                }
                else -> {
                    finish()
                }
            }
            val args = Bundle().apply {
                putParcelable(ARG_MEDIA_PICKER_EXTRAS, mediaPickerExtras)
            }
            navController.setGraph(navGraph, args)
        }
    }

    /**
     * If Media Picker type is Pdf and device version is >= Q(29), then show system app picker.
     * This is done due to storage restrictions for non-media files in Android 10.
     * */
    private fun checkIfDocumentPickerInitiated(): Boolean {
        if (MediaType.isPDF(mediaPickerExtras.mediaTypes)
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            val intent = AndroidUtil.getExternalDocumentPickerIntent(
                allowMultipleSelect = mediaPickerExtras.allowMultipleSelect
            )
            browserMediaLauncher.launch(intent)
            return true
        }
        return false
    }

    private fun showPickDocumentsListScreen(
        vararg mediaUris: SingleUriData,
        saveInCache: Boolean = false,
        isExternallyShared: Boolean = false,
    ) {
        val attachments = if (saveInCache) {
            AndroidUtil.moveAttachmentToCache(this, *mediaUris)
        } else {
            mediaUris.asList()
        }

        val arrayList = ArrayList<SingleUriData>()
        arrayList.addAll(attachments)

        val mediaExtras = MediaExtras.Builder()
            .mediaScreenType(MEDIA_DOCUMENT_SEND_SCREEN)
            .mediaUris(arrayList)
            .text(mediaPickerExtras.text)
            .isExternallyShared(isExternallyShared)
            .build()
        if (attachments.isNotEmpty()) {
            val intent = MediaActivity.getIntent(this, mediaExtras)
            documentSendLauncher.launch(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionManager.REQUEST_CODE_SETTINGS_PERMISSION) {
            checkStoragePermission()
        }
    }

    override fun onBackPressed() {
        when (val fragment = supportFragmentManager.currentFragment(R.id.nav_host)) {
            is MediaPickerFolderFragment -> {
                super.onBackPressed()
            }
            is MediaPickerItemFragment -> {
                fragment.onBackPressedFromFragment()
            }
            is MediaPickerDocumentFragment -> {
                if (fragment.onBackPressedFromFragment()) super.onBackPressed()
            }
            is MediaPickerAudioFragment -> {
                if (fragment.onBackPress()) super.onBackPressed()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}