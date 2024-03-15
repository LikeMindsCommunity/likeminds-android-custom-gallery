package com.likeminds.customgallery.utils.permissions

interface PermissionCallback {
    fun onGrant()
    fun onDeny()
}