package com.likeminds.customgallery.utils.actionmode

import android.view.MenuItem

interface ActionModeListener {
    fun onActionItemClick(item: MenuItem?)
    fun onActionModeDestroyed()
}