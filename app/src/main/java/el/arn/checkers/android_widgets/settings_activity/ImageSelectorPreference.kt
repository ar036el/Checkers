package el.arn.checkers.android_widgets.settings_activity

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import el.arn.checkers.R
import el.arn.checkers.complementaries.android.ALPHA_ICON_DISABLED_AS_FLOAT
import el.arn.checkers.complementaries.android.ALPHA_ICON_ENABLED_AS_FLOAT
import el.arn.checkers.complementaries.listener_mechanism.HoldsListeners
import el.arn.checkers.complementaries.listener_mechanism.ListenersManager

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

    override var isLockEnabled: Boolean = true

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
) : ImageSelectorPreferenceImpl(imageViewsIDs, 0, 1, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(
            R.drawable.piece_both_players_1,
            R.drawable.piece_both_players_2,
            R.drawable.piece_both_players_3
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
            R.drawable.board_theme_1,
            R.drawable.board_theme_2,
            R.drawable.board_theme_3,
            R.drawable.board_theme_4
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
            R.drawable.sound_effects_theme_a,
            R.drawable.sound_effects_theme_b,
            R.drawable.sound_effects_theme_c
        )
    }
}