package el.arn.opencheckers

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import el.arn.opencheckers.checkers_game.game_core.configurations.BoardConfig

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
            boardSizePref = findPreference(resources.getString(R.string.pref_board_size))!!
            customBoardSizePref = findPreference(resources.getString(R.string.pref_custom_board_size))!!
            startingRowsPref = findPreference(resources.getString(R.string.pref_custom_starting_rows))!!
            kingBehaviourPreference = findPreference(resources.getString(R.string.pref_king_behaviour))!!
            enableCustomSettingsPref = findPreference(resources.getString(R.string.pref_enable_custom_settings))!!
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = resources.getString(R.string.prefFile_settings)
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
            val maxStartingRows = Application.boardConfig.maxStartingRowsForEachPlayer(boardSize)
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
) : ImageSelectorPreference(imageViewsIDs, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(1, 2, 3)
    }
}

class BoardThemeSelectorPreference@JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : ImageSelectorPreference(imageViewsIDs, context, attrs, defStyleAttr, defStyleRes) {
    companion object {
        val imageViewsIDs = intArrayOf(1, 2, 3)
    }
}

open class ImageSelectorPreference @JvmOverloads constructor(
    private val imageViewsIDs: IntArray,
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle,
    defStyleRes: Int = 0
) :
    Preference(context, attrs, defStyleAttr, defStyleRes) {
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        //holder.itemView.setClickable(false); // disable parent click
        val button = holder.findViewById(R.id.theme_light)
        button.isClickable = true // enable custom view click
        button.setOnClickListener {
            // persist your value here
        }

        // the rest of the click binding
    }

    init {
        widgetLayoutResource = R.layout.element_pref_theme_switch_widget
    }
}