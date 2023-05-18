package com.likeminds.customgallery.media.model

import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

internal data class LocalAppData(
    val appId: Int,
    val appName: String,
    val appIcon: Drawable,
    val resolveInfo: ResolveInfo
)
