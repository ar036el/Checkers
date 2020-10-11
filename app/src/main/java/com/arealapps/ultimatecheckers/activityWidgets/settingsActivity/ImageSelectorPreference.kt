/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.ultimatecheckers.activityWidgets.settingsActivity

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.arealapps.ultimatecheckers.R
import com.arealapps.ultimatecheckers.helpers.consts.ALPHA_ICON_DISABLED_AS_FLOAT
import com.arealapps.ultimatecheckers.helpers.consts.ALPHA_ICON_ENABLED_AS_FLOAT
import com.arealapps.ultimatecheckers.helpers.listeners_engine.HoldsListeners
import com.arealapps.ultimatecheckers.helpers.listeners_engine.ListenersManager

interface ImageSelectorPreference : HoldsListeners<ImageSelectorPreference.Listener> {

    var isLockEnabled: Boolean
    val currentImageIndex: Int
    val savedValueAsSelectedImageIndex: Int
    val isCurrentImageLocked: Boolean


    interface Listener {
        fun imageWasChanged(imageSelectorPreference: ImageSelectorPreference, currentImageIndex: Int)
    }

}

open class ImageSelectorPreferenceImpl @JvmOverloads constructor(
    private val imagesAsResId: IntArray,
    private val defaultImageIndex: Int,
    private val lockedFromIndex: Int,
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0,
    private val listenersMgr: ListenersManager<ImageSelectorPreference.Listener> = ListenersManager()
) : ImageSelectorPreference, Preference(context, attrs, defStyleAttr, defStyleRes), HoldsListeners<ImageSelectorPreference.Listener> by listenersMgr {

    private lateinit var prev: ImageButton
    private lateinit var next: ImageButton
    private lateinit var image: ImageButton
    private lateinit var lock: ImageView

    override var isLockEnabled: Boolean = false //lateinit
        set(value) {
            field = value
            resetSavedValueIfIsLocked()
        }

    override var currentImageIndex = 0
        set(value) {
            field = value
            if (!isCurrentImageLocked) {
                savedValueAsSelectedImageIndex = value
            }
        }

    override var savedValueAsSelectedImageIndex
        get() = sharedPreferences.getInt(key, defaultImageIndex)
        set(value) = with (sharedPreferences.edit()) {
            putInt(key, value)
            commit()
        }

    override val isCurrentImageLocked: Boolean get() = (isLockEnabled && currentImageIndex >= lockedFromIndex)


    init {
        widgetLayoutResource = R.layout.element_pref_image_selector
        isLockEnabled = false
    }

    private fun resetSavedValueIfIsLocked() {
        if (isLockEnabled && savedValueAsSelectedImageIndex >= lockedFromIndex) {
            savedValueAsSelectedImageIndex = lockedFromIndex-1
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        initPreferenceView(holder)
    }

    private fun initPreferenceView(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.isClickable = false; //disable general click that come from parent

        prev = holder.findViewById(R.id.imageSelectorPrefWidget_back) as ImageButton
        next = holder.findViewById(R.id.imageSelectorPrefWidget_next) as ImageButton
        image = holder.findViewById(R.id.imageSelectorPrefWidget_image) as ImageButton
        lock = holder.findViewById(R.id.imageSelectorPrefWidget_lock) as ImageView

        prev.setOnClickListener { prev() }
        next.setOnClickListener { next() }
        image.setOnClickListener { next() }

        currentImageIndex = savedValueAsSelectedImageIndex
        restoreCurrentImageIndexIfItGotOutOfBoundsBySomeReason()

        updateViewComponents()
    }

    private fun restoreCurrentImageIndexIfItGotOutOfBoundsBySomeReason() {
        if (currentImageIndex < 0 || currentImageIndex > imagesAsResId.lastIndex) {
            currentImageIndex = defaultImageIndex
        }
    }


    private fun updateViewComponents() {
        prev.isClickable = hasPrev()
        prev.alpha = if (hasPrev()) ALPHA_ICON_ENABLED_AS_FLOAT else ALPHA_ICON_DISABLED_AS_FLOAT

        next.isClickable = hasNext()
        next.alpha = if (hasNext()) ALPHA_ICON_ENABLED_AS_FLOAT else ALPHA_ICON_DISABLED_AS_FLOAT

        image.isClickable = hasNext()
        image.setImageResource(imagesAsResId[currentImageIndex])
        listenersMgr.notifyAll { it.imageWasChanged(this, currentImageIndex) }

        lock.visibility = if (currentImageIndex >= lockedFromIndex && isLockEnabled) View.VISIBLE else View.INVISIBLE
    }

    private fun hasNext() = (currentImageIndex != imagesAsResId.lastIndex)

    private fun hasPrev() = (currentImageIndex != 0)

    private fun next() {
        currentImageIndex++
        updateViewComponents()
    }

    private fun prev() {
        val currentImageIndex = sharedPreferences.getInt(key, defaultImageIndex)
        this.currentImageIndex--
        updateViewComponents()
    }

}

class PlayerThemeSelectorPreference@JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : ImageSelectorPreferenceImpl(imageViewsIDs, 0, 3, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(
            R.drawable.piece_both_players_0,
            R.drawable.piece_both_players_1,
            R.drawable.piece_both_players_2,
            R.drawable.piece_both_players_3,
            R.drawable.piece_both_players_4,
            R.drawable.piece_both_players_5,
            R.drawable.piece_both_players_6,
            R.drawable.piece_both_players_7,
            R.drawable.piece_both_players_8,
            R.drawable.piece_both_players_9
        )
    }
}

class BoardThemeSelectorPreference@JvmOverloads constructor( //todo put this and others outside?
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : ImageSelectorPreferenceImpl(imageViewsIDs, 0, 2, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(
            R.drawable.board_theme_0,
            R.drawable.board_theme_1,
            R.drawable.board_theme_2,
            R.drawable.board_theme_3,
            R.drawable.board_theme_4,
            R.drawable.board_theme_5
        )
    }
}

class SoundEffectsThemeSelectorPreference@JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : ImageSelectorPreferenceImpl(imageViewsIDs, 1, 2, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(
            R.drawable.sound_effects_theme_no_sound,
            R.drawable.sound_effects_theme_0,
            R.drawable.sound_effects_theme_1,
            R.drawable.sound_effects_theme_2
        )
    }
}