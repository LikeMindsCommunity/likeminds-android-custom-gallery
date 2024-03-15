package com.likeminds.customgallery.utils.actionmode

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode

class ActionModeCallback : ActionMode.Callback {

    var actionModeListener: ActionModeListener? = null

    private var mode: ActionMode? = null

    @MenuRes
    private var menuResId: Int = 0
    private var title: String? = null

    private var isVisible: Boolean = false

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let {
            this.mode = mode
            mode.menuInflater.inflate(menuResId, menu)
            mode.title = title
        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        actionModeListener?.onActionItemClick(item)
        mode?.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        isVisible = false
        this.mode = null
        actionModeListener?.onActionModeDestroyed()
    }

    fun startActionMode(
        actionModeListener: ActionModeListener,
        view: AppCompatActivity,
        @MenuRes menuResId: Int,
        title: String? = null
    ) {
        this.actionModeListener = actionModeListener
        this.menuResId = menuResId
        this.title = title
        view.startSupportActionMode(this)
        isVisible = true
    }

    fun finishActionMode() {
        isVisible = false
        mode?.finish()
    }

    fun updateTitle(updatedTitle: String) {
        mode?.title = updatedTitle
    }

    fun isActionModeEnabled(): Boolean {
        return isVisible
    }
}