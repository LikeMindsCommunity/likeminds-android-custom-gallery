package com.likeminds.customgallery.utils.permissions

import android.os.Build
import com.likeminds.customgallery.utils.customview.BaseAppCompatActivity

class PermissionManager {

    companion object {
        const val REQUEST_CODE_SETTINGS_PERMISSION = 100

        fun performTaskWithPermissionExtras(
            activity: BaseAppCompatActivity,
            task: PermissionTask,
            permissionExtras: PermissionExtras,
            showInitialPopup: Boolean,
            showDeniedPopup: Boolean,
            setInitialPopupDismissible: Boolean = false,
            setDeniedPopupDismissible: Boolean = false,
            permissionDeniedCallback: PermissionDeniedCallback? = null,
        ) {
            val permissions = permissionExtras.permissions
            if (activity.hasPermissions(permissions)) {
                task.doTask()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.canRequestPermissions(permissions)) {
                        if (showInitialPopup) {
                            val permissionDialog = PermissionDialog(
                                activity,
                                task,
                                null,
                                PermissionDialog.Mode.INIT,
                                permissionDeniedCallback,
                                permissionExtras
                            )
                            permissionDialog.setCanceledOnTouchOutside(setInitialPopupDismissible)
                            permissionDialog.show()
                        } else {
                            activity.requestMultiplePermissions(
                                permissionExtras,
                                object : PermissionCallback {
                                    override fun onGrant() {
                                        task.doTask()
                                    }

                                    override fun onDeny() {
                                        if (showDeniedPopup) {
                                            val permissionDialog = PermissionDialog(
                                                activity,
                                                task,
                                                null,
                                                PermissionDialog.Mode.DENIED,
                                                permissionDeniedCallback,
                                                permissionExtras
                                            )
                                            permissionDialog.setCanceledOnTouchOutside(
                                                setDeniedPopupDismissible
                                            )
                                            permissionDialog.setCancelable(setDeniedPopupDismissible)
                                            permissionDialog.show()
                                        } else {
                                            permissionDeniedCallback?.onDeny()
                                        }
                                    }
                                })
                        }
                    } else {
                        if (showDeniedPopup) {
                            val permissionDialog = PermissionDialog(
                                activity,
                                task,
                                null,
                                PermissionDialog.Mode.DENIED,
                                permissionDeniedCallback,
                                permissionExtras
                            )
                            permissionDialog.setCanceledOnTouchOutside(setDeniedPopupDismissible)
                            permissionDialog.setCancelable(setDeniedPopupDismissible)
                            permissionDialog.show()
                        } else {
                            permissionDeniedCallback?.onDeny()
                        }
                    }
                }
            }
        }

        fun performTaskWithPermission(
            activity: BaseAppCompatActivity,
            task: PermissionTask,
            permission: Permission,
            showInitialPopup: Boolean,
            showDeniedPopup: Boolean,
            setInitialPopupDismissible: Boolean = false,
            setDeniedPopupDismissible: Boolean = false,
            permissionDeniedCallback: PermissionDeniedCallback? = null,
        ) {
            if (activity.hasPermission(permission))
                task.doTask()
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.canRequestPermission(permission.permissionName)) {
                        if (showInitialPopup) {
                            val permissionDialog = PermissionDialog(
                                activity,
                                task,
                                permission,
                                PermissionDialog.Mode.INIT,
                                permissionDeniedCallback
                            )
                            permissionDialog.setCanceledOnTouchOutside(setInitialPopupDismissible)
                            permissionDialog.show()
                        } else {
                            activity.requestPermission(permission, object : PermissionCallback {
                                override fun onGrant() {
                                    task.doTask()
                                }

                                override fun onDeny() {
                                    if (showDeniedPopup) {
                                        val permissionDialog = PermissionDialog(
                                            activity,
                                            task,
                                            permission,
                                            PermissionDialog.Mode.DENIED,
                                            permissionDeniedCallback
                                        )
                                        permissionDialog.setCanceledOnTouchOutside(
                                            setDeniedPopupDismissible
                                        )
                                        permissionDialog.setCancelable(setDeniedPopupDismissible)
                                        permissionDialog.show()
                                    } else {
                                        permissionDeniedCallback?.onDeny()
                                    }
                                }
                            })
                        }
                    } else {
                        if (showDeniedPopup) {
                            val permissionDialog = PermissionDialog(
                                activity,
                                task,
                                permission,
                                PermissionDialog.Mode.DENIED,
                                permissionDeniedCallback
                            )
                            permissionDialog.setCanceledOnTouchOutside(setDeniedPopupDismissible)
                            permissionDialog.setCancelable(setDeniedPopupDismissible)
                            permissionDialog.show()
                        } else {
                            permissionDeniedCallback?.onDeny()
                        }
                    }
                }
            }
        }
    }
}
