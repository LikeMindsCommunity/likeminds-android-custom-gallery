package com.likeminds.customgallery.media.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.R
import com.likeminds.customgallery.media.MediaRepository
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallery.media.util.MediaUtils
import com.likeminds.customgallery.media.view.MediaActivity.Companion.BUNDLE_MEDIA_EXTRAS
import com.likeminds.customgallery.utils.AndroidUtil
import com.likeminds.customgallery.utils.ViewUtils.currentFragment
import com.likeminds.customgallery.utils.customview.BaseAppCompatActivity
import com.likeminds.customgallery.utils.permissions.*

internal class MediaPickerActivity : BaseAppCompatActivity() {

    private lateinit var mediaPickerExtras: MediaPickerExtras

    companion object {
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
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val uris = MediaUtils.getExternalIntentPickerUris(result.data)
                    val mediaRepository = MediaRepository()
                    mediaRepository.getLocalUrisDetails(this, uris) {
                        val mediaUris = MediaUtils.convertMediaViewDataToSingleUriData(
                            this, it
                        )
                        if (mediaUris.isNotEmpty() && mediaPickerExtras.isEditingAllowed) {
                            showPickDocumentsListScreen(mediaUris)
                        } else {
                            setResultAndFinish(
                                mediaUris,
                                mediaPickerExtras.text
                            )
                        }
                    }
                }

                Activity.RESULT_CANCELED -> {
                    finish()
                }

                Activity.RESULT_FIRST_USER -> {
                    finish()
                }
            }
        }

    private val documentSendLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data =
                    result.data?.extras?.getParcelable<MediaExtras>(BUNDLE_MEDIA_EXTRAS)
                        ?: return@registerForActivityResult

                setResultAndFinish(
                    data.mediaUris?.toList() ?: listOf(),
                    data.text
                )
            } else if (result?.resultCode == Activity.RESULT_FIRST_USER) {
                finish()
            }
        }

    private fun setResultAndFinish(
        media: List<SingleUriData>,
        text: String?
    ) {
        val resultIntent = CustomGallery.getResultIntent(
            mediaPickerExtras.mediaTypes,
            media,
            text
        )
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_picker)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host)) { view, windowInsets ->
            val innerPadding = windowInsets.getInsets(
                // Notice we're using systemBars, not statusBar
                WindowInsetsCompat.Type.systemBars()
                        // Notice we're also accounting for the display cutouts
                        or WindowInsetsCompat.Type.displayCutout()
                        // If using EditText, also add
                        or WindowInsetsCompat.Type.ime()
            )
            // Apply the insets as padding to the view. Here, set all the dimensions
            // as appropriate to your layout. You can also update the view's margin if
            // more appropriate.
            view.setPadding(0, innerPadding.top, 0, innerPadding.bottom)

            // Return CONSUMED if you don't want the window insets to keep passing down
            // to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        val extras = intent.extras?.getParcelable<MediaPickerExtras>(ARG_MEDIA_PICKER_EXTRAS)
        if (extras == null) {
            throw IllegalArgumentException("Arguments are missing")
        } else {
            mediaPickerExtras = extras
        }

        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val mediaTypes = mediaPickerExtras.mediaTypes
            if (mediaTypes.contains(PDF) || mediaTypes.contains(GIF)) {
                startMediaPickerFragment()
                return
            }
            val permissionExtras = Permission.getGalleryPermissionExtras(this)

            PermissionManager.performTaskWithPermissionExtras(
                this,
                { startMediaPickerFragment() },
                permissionExtras,
                showInitialPopup = true,
                showDeniedPopup = true,
                permissionDeniedCallback = object : PermissionDeniedCallback {
                    override fun onDeny() {
                        onBackPressedDispatcher.onBackPressed()
                    }

                    override fun onCancel() {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            )
        } else {
            PermissionManager.performTaskWithPermission(
                this,
                { startMediaPickerFragment() },
                Permission.getStoragePermissionData(),
                showInitialPopup = true,
                showDeniedPopup = true,
                permissionDeniedCallback = object : PermissionDeniedCallback {
                    override fun onDeny() {
                        onBackPressedDispatcher.onBackPressed()
                    }

                    override fun onCancel() {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            )
        }
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
        } else {
//            setResult(Activity.RESULT_OK, intent)
//            finish()
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
        medias: List<SingleUriData>,
        saveInCache: Boolean = false,
        isExternallyShared: Boolean = false,
    ) {
        val attachments = if (saveInCache) {
            AndroidUtil.moveAttachmentToCache(this, *medias.toTypedArray())
        } else {
            medias
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