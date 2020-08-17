package el.arn.opencheckers.activities

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import el.arn.opencheckers.R
import el.arn.opencheckers.android_widgets.settings_activity.BoardThemeSelectorPreference
import el.arn.opencheckers.android_widgets.settings_activity.ImageSelectorPreference
import el.arn.opencheckers.android_widgets.settings_activity.PlayerThemeSelectorPreference
import el.arn.opencheckers.appRoot
import el.arn.opencheckers.game.game_core.game_core.configurations.BoardConfig
import el.arn.opencheckers.game.game_core.game_core.implementations.BoardConfigImpl

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsFragment =
            SettingsFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, settingsFragment)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }



    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var regularBoardSizePref: ListPreference
        private lateinit var customBoardSizePref: ListPreference
        private lateinit var startingRowsPref: ListPreference
        private lateinit var enableCustomSettingsPref: SwitchPreferenceCompat
        private lateinit var boardThemePref: BoardThemeSelectorPreference
        private lateinit var playersThemePref: PlayerThemeSelectorPreference

        private var arePremiumFeaturedLocked = true //lateinit


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "settings"
            setPreferencesFromResource(R.xml.settings, rootKey)

            arePremiumFeaturedLocked = !appRoot.purchasesManager.purchasedFullVersion

            findPreferences()
            initCustomSettingsPreferences()
            initImageSelectorPreferences()

        }

        private fun findPreferences() {
            regularBoardSizePref = findPreference("boardSizeRegular")!!
            customBoardSizePref = findPreference("boardSizeCustom")!!
            startingRowsPref = findPreference("startingRows")!!
            enableCustomSettingsPref = findPreference("isCustomSettingsEnabled")!!
            boardThemePref = findPreference("boardTheme")!!
            playersThemePref = findPreference("playersTheme")!!
        }

        private fun initCustomSettingsPreferences() {
            val isCustomSettingsEnabled =  if (arePremiumFeaturedLocked) false else preferenceManager.sharedPreferences.getBoolean(enableCustomSettingsPref.key, false)
            enableOrDisableCustomSettingsPref(isCustomSettingsEnabled)

            enableCustomSettingsPref.setOnPreferenceChangeListener {
                _, isChecked ->
                val isChecked = isChecked as Boolean
                if (isChecked && arePremiumFeaturedLocked) {
                    Handler().postDelayed({
                        appRoot.toastMessageManager.showShort(appRoot.getStringRes(R.string.settings_toastMessage_premiumFeature))
                        enableOrDisableCustomSettingsPref(false)
                    },200)
                }
                enableOrDisableCustomSettingsPref(isChecked)
                true
            }

            regularBoardSizePref.setOnPreferenceChangeListener { _, boardSize -> updateStartingRowsPrefEntries(boardSize.toString().toInt()); true}
            customBoardSizePref.setOnPreferenceChangeListener {_, boardSize -> updateStartingRowsPrefEntries(boardSize.toString().toInt()); true}
        }

        private fun enableOrDisableCustomSettingsPref(isEnabled: Boolean) {
            enableCustomSettingsPref.isChecked = isEnabled
            startingRowsPref.isVisible = isEnabled
            customBoardSizePref.isVisible = isEnabled
            regularBoardSizePref.isEnabled = !isEnabled
            startingRowsPref.isEnabled = !arePremiumFeaturedLocked
            customBoardSizePref.isEnabled = !arePremiumFeaturedLocked

            updateStartingRowsPrefEntries(isEnabled)
        }

        private fun initImageSelectorPreferences() {
            boardThemePref.isLockEnabled = arePremiumFeaturedLocked
            playersThemePref.isLockEnabled = arePremiumFeaturedLocked
            boardThemePref.addListener(imageSelectorPreferenceListener)
            playersThemePref.addListener(imageSelectorPreferenceListener)
        }

        private val imageSelectorPreferenceListener = object : ImageSelectorPreference.Listener {
            override fun imageWasChanged(imageSelectorPreference: ImageSelectorPreference, currentImageIndex: Int) {
                if (imageSelectorPreference.isCurrentImageLocked) {
                    appRoot.toastMessageManager.showShort(appRoot.getStringRes(R.string.settings_toastMessage_premiumTheme))
                }
            }
        }

        private fun updateStartingRowsPrefEntries(isCustomSettingsEnabled: Boolean) {
            updateStartingRowsPrefEntries(
                preferenceManager.sharedPreferences.getString(
                    if (isCustomSettingsEnabled) {
                        customBoardSizePref.key
                    } else {
                        regularBoardSizePref.key
                    }, "8")!!.toInt()
            )
        }

        private fun updateStartingRowsPrefEntries(boardSize: Int) {
            val isCustomSettingsEnabled =  preferenceManager.sharedPreferences.getBoolean(enableCustomSettingsPref.key, false)

            val minStartingRows = BoardConfig.minStartingRowsForEachPlayer
            val maxStartingRows = BoardConfigImpl().maxStartingRowsForEachPlayer(boardSize)
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