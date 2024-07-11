package com.likeminds.customgallery.utils.customview

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.customgallery.utils.permissions.*
import com.likeminds.customgallery.utils.permissions.Permission.Companion.READ_MEDIA_VISUAL_USER_SELECTED
import com.likeminds.customgallery.utils.permissions.Permission.Companion.REQUEST_GALLERY

open class BaseAppCompatActivity : AppCompatActivity() {
    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are *not* resumed.
     */

    private lateinit var sessionPermission: SessionPermission
    private val permissionCallbackSparseArray = SparseArray<PermissionCallback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionPermission = SessionPermission(applicationContext)
    }

    fun hasPermission(permission: Permission): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            checkSelfPermission(permission.permissionName) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPermissions(permissions: Array<String>): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            var hasPermission = true
            var isPartialMediaPermission = false
            permissions.forEach { permission ->
                if (permission == READ_MEDIA_VISUAL_USER_SELECTED
                    && checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                ) {
                    isPartialMediaPermission = true
                    return@forEach
                }
                hasPermission =
                    hasPermission && checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }
            return hasPermission || isPartialMediaPermission
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestPermission(permission: Permission, permissionCallback: PermissionCallback) {
        permissionCallbackSparseArray.put(permission.requestCode, permissionCallback)
        sessionPermission.setPermissionRequest(permission.permissionName)
        requestPermissions(arrayOf(permission.permissionName), permission.requestCode)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestMultiplePermissions(
        permissionExtras: PermissionExtras,
        permissionCallback: PermissionCallback
    ) {
        permissionExtras.apply {
            permissions.forEach { permissionName ->
                permissionCallbackSparseArray.put(requestCode, permissionCallback)
                sessionPermission.setPermissionRequest(permissionName)
            }
            requestPermissions(permissions, requestCode)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun canRequestPermissions(permissions: Array<String>): Boolean {
        var canRequest = true
        permissions.forEach { permission ->
            canRequest = canRequest && (!wasRequestedBefore(permission) ||
                    shouldShowRequestPermissionRationale(permission))
        }
        return canRequest
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun canRequestPermission(permissionName: String): Boolean {
        return !wasRequestedBefore(permissionName) ||
                shouldShowRequestPermissionRationale(permissionName)
    }

    private fun wasRequestedBefore(permissionName: String): Boolean {
        return sessionPermission.wasPermissionRequestedBefore(permissionName)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = permissionCallbackSparseArray.get(requestCode, null) ?: return
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callback.onGrant()
            } else if (requestCode == REQUEST_GALLERY
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                && checkSelfPermission(READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
            ) {
                //if the API version >= 34 and the request code is REQUEST_GALLERY then we check if the READ_MEDIA_VISUAL_USER_SELECTED permission is granted
                callback.onGrant()
            } else {
                callback.onDeny()
            }
        } else {
            callback.onDeny()
        }
    }
}