package com.likeminds.customgallery.utils.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.likeminds.customgallery.R

class Permission private constructor(
    val permissionName: String,
    val requestCode: Int,
    val preDialogMessage: String,
    val deniedDialogMessage: String,
    @param:DrawableRes @field:DrawableRes
    val dialogImage: Int
) {
    companion object {
        private const val WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        const val READ_MEDIA_VISUAL_USER_SELECTED =
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

        private const val REQUEST_STORAGE = 10102
        private const val REQUEST_RECORD_AUDIO = 10103
        const val REQUEST_GALLERY = 10106
        private const val REQUEST_AUDIO = 10107

        fun getStoragePermissionData(): Permission {
            return Permission(
                WRITE_STORAGE,
                REQUEST_STORAGE,
                "To easily receive and send photos, videos and other files, allow LikeMinds access to your device’s photos, media and files.",
                "To send media, allow LikeMinds access to your device’s photos, media and files. Tap on Settings > Permission, and turn Storage on.",
                R.drawable.ic_folder
            )
        }

        // returns the [PermissionExtras] for gallery permissions request
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun getGalleryPermissionExtras(context: Context): PermissionExtras {
            val permissionsArray =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    arrayOf(
                        READ_MEDIA_IMAGES,
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_VISUAL_USER_SELECTED
                    )
                } else {
                    arrayOf(
                        READ_MEDIA_VIDEO,
                        READ_MEDIA_IMAGES
                    )
                }

            return PermissionExtras.Builder()
                .permissions(permissionsArray)
                .requestCode(REQUEST_GALLERY)
                .preDialogMessage(context.getString(R.string.pre_gallery_media_permission_dialog_message))
                .deniedDialogMessage(context.getString(R.string.denied_gallery_media_permission_dialog_message))
                .dialogImage(R.drawable.ic_folder)
                .build()
        }
    }
}
