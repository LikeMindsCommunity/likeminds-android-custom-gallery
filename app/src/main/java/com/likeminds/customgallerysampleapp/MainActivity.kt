package com.likeminds.customgallerysampleapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.likeminds.customgallery.CustomGallery
import com.likeminds.customgallery.media.model.*
import com.likeminds.customgallerysampleapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras?.getParcelable<CustomGalleryResult>(
                    CustomGallery.ARG_CUSTOM_GALLERY_RESULT
                ) ?: return@registerForActivityResult

                Log.d("PUI", ": ${data.medias.size} ${data.medias[0].uri}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
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

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.apply {
            layoutAddImage.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(IMAGE))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutAddVideo.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(VIDEO))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutAttachFiles.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(PDF))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutAttachAudio.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(AUDIO))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }

            layoutGallery.setOnClickListener {
                CustomGallery.start(
                    launcher,
                    this@MainActivity,
                    CustomGalleryConfig.Builder()
                        .mediaTypes(listOf(IMAGE, VIDEO))
                        .allowMultipleSelect(true)
                        .isEditingEnabled(true)
                        .build()
                )
            }
        }
    }
}