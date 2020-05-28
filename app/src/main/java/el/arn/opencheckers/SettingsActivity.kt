package el.arn.opencheckers

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import el.arn.opencheckers.checkers_game.game_core.configurations.BoardConfig

const val ALPHA_INACTIVE = 0.6f
const val ALPHA_DISABLED = 0.38f

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsFragment = SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, settingsFragment)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }



    class SettingsFragment : PreferenceFragmentCompat() {

        lateinit var boardSizePref: ListPreference
        lateinit var customBoardSizePref: ListPreference
        lateinit var startingRowsPref: ListPreference
        lateinit var kingBehaviourPreference: ListPreference
        lateinit var enableCustomSettingsPref: SwitchPreferenceCompat

        private fun findPrefs() {
            boardSizePref = findPreference(resources.getString(R.string.pref_boardSize))!!
            customBoardSizePref = findPreference(resources.getString(R.string.pref_customBoardSize))!!
            startingRowsPref = findPreference(resources.getString(R.string.pref_customStartingRows))!!
            kingBehaviourPreference = findPreference(resources.getString(R.string.pref_kingBehaviour))!!
            enableCustomSettingsPref = findPreference(resources.getString(R.string.pref_customSettingsEnabled))!!
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = resources.getString(R.string.prefCategory_main)
            setPreferencesFromResource(R.xml.settings, rootKey)
            findPrefs()
            initCustomPrefs()
        }

        private fun initCustomPrefs() {
            val isCustomSettingsEnabled =  preferenceManager.sharedPreferences.getBoolean(enableCustomSettingsPref.key, false)
            updateCustomSettingPrefsVisibility(isCustomSettingsEnabled)
            updateStartingRowsPrefEntries(isCustomSettingsEnabled)

            enableCustomSettingsPref.setOnPreferenceChangeListener { _, isSwitchedOn ->
                updateCustomSettingPrefsVisibility(isSwitchedOn as Boolean)
                updateStartingRowsPrefEntries(isSwitchedOn)
                true }

            boardSizePref.setOnPreferenceChangeListener {_, boardSize -> updateStartingRowsPrefEntries(boardSize.toString().toInt()); true}
            customBoardSizePref.setOnPreferenceChangeListener {_, boardSize -> updateStartingRowsPrefEntries(boardSize.toString().toInt()); true}

        }

        fun updateCustomSettingPrefsVisibility(isCustomSettingsEnabled: Boolean) {
            startingRowsPref.isVisible = isCustomSettingsEnabled
            customBoardSizePref.isVisible = isCustomSettingsEnabled
            boardSizePref.isEnabled = !isCustomSettingsEnabled
        }

        private fun updateStartingRowsPrefEntries(isCustomSettingsEnabled: Boolean) {
            updateStartingRowsPrefEntries(
                preferenceManager.sharedPreferences.getString(
                    if (isCustomSettingsEnabled) {
                        customBoardSizePref.key
                    } else {
                        boardSizePref.key
                    }, "8")!!.toInt()
            )
        }

        fun updateStartingRowsPrefEntries(boardSize: Int) {
            val isCustomSettingsEnabled =  preferenceManager.sharedPreferences.getBoolean(enableCustomSettingsPref.key, false)

            val minStartingRows = BoardConfig.minStartingRowsForEachPlayer
            val maxStartingRows = App.boardConfig.maxStartingRowsForEachPlayer(boardSize)
            val entries = Array(maxStartingRows - minStartingRows + 1) { (it +  minStartingRows).toString() }
            startingRowsPref.entries = entries
            startingRowsPref.entryValues = entries

            if (startingRowsPref.value?.toInt() !in minStartingRows..maxStartingRows) {
                startingRowsPref.value =
                    if (isCustomSettingsEnabled && boardSize == 4) {
                        "1"
                    } else if (isCustomSettingsEnabled && boardSize == 6) {
                        "2"
                    } else {
                        "3"
                    }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}

class PlayerThemeSelectorPreference@JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : ImageSelectorPreference(imageViewsIDs, 0, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(
            R.drawable.piece_both_players_1,
            R.drawable.piece_both_players_2,
            R.drawable.piece_both_players_3)
    }
}

class BoardThemeSelectorPreference@JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : ImageSelectorPreference(imageViewsIDs, 0, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(
            R.drawable.board_theme_1,
            R.drawable.board_theme_2,
            R.drawable.board_theme_3,
            R.drawable.board_theme_4)
    }
}

open class ImageSelectorPreference @JvmOverloads constructor(
    private val imagesResId: IntArray,
    private val defaultValue: Int,
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var prev: ImageButton
    private lateinit var next: ImageButton
    private lateinit var image: ImageButton

    init {
        widgetLayoutResource = R.layout.element_pref_image_selector
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.isClickable = false; // disable parent click

        prev = holder.findViewById(R.id.imageSelectorPrefWidget_back) as ImageButton
        next = holder.findViewById(R.id.imageSelectorPrefWidget_next) as ImageButton
        image = holder.findViewById(R.id.imageSelectorPrefWidget_image) as ImageButton
        prev.setOnClickListener { prev() }
        next.setOnClickListener { next() }
        image.setOnClickListener { next() }

        if (value < 0 || value > imagesResId.lastIndex) {
            value = defaultValue
        }

        updateElements()
    }

    private fun updateElements() {
        prev.isClickable = hasPrev()
        prev.alpha = if (hasPrev()) 1f else ALPHA_DISABLED

        next.isClickable = hasNext()
        next.alpha = if (hasNext()) 1f else ALPHA_DISABLED

        image.isClickable = hasNext()
        image.setImageResource(imagesResId[value])
    }

    private fun hasNext() = (value != imagesResId.lastIndex)
    private fun next() {
        value++
        updateElements()
    }

    private fun hasPrev() = (value != 0)
    private fun prev() {
        val currentImageIndex = sharedPreferences.getInt(key, defaultValue)
        value--
        updateElements()
    }

    private var value
        get() = sharedPreferences.getInt(key, defaultValue)
        set(value) = with (sharedPreferences.edit()) {
            putInt(key, value)
            commit()
        }

}