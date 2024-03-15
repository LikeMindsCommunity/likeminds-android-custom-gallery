package com.likeminds.customgallery.utils.customview

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.likeminds.customgallery.utils.permissions.Permission
import com.likeminds.customgallery.utils.permissions.PermissionCallback
import com.likeminds.customgallery.utils.permissions.SessionPermission

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestPermission(permission: Permission, permissionCallback: PermissionCallback) {
        permissionCallbackSparseArray.put(permission.requestCode, permissionCallback)
        sessionPermission.setPermissionRequest(permission)
        requestPermissions(arrayOf(permission.permissionName), permission.requestCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun canRequestPermission(permission: Permission): Boolean {
        return !wasRequestedBefore(permission) ||
                shouldShowRequestPermissionRationale(permission.permissionName)
    }

    private fun wasRequestedBefore(permission: Permission): Boolean {
        return sessionPermission.wasPermissionRequestedBefore(permission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = permissionCallbackSparseArray.get(requestCode, null) ?: return
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callback.onGrant()
            } else {
                callback.onDeny()
            }
        } else {
            callback.onDeny()
        }
    }
}