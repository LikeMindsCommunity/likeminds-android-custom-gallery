package com.likeminds.customgallery.utils.permissions

import android.content.Context
import android.os.Build

class SessionPermission constructor(applicationContext: Context) {
    companion object {
        const val PERMISSION_PREFS = "permission_prefs"
    }

    private val permissionPreferences by lazy {
        applicationContext.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
    }

    fun setPermissionRequest(permissionName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            permissionPreferences.edit().putBoolean(permissionName, true).apply()
        }
    }

    fun wasPermissionRequestedBefore(permissionName: String): Boolean {
        return permissionPreferences.getBoolean(permissionName, false)
    }
}
