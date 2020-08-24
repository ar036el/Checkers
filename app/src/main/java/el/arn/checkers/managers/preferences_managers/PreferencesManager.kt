package el.arn.checkers.managers.preferences_managers

import android.content.SharedPreferences
import el.arn.checkers.helpers.listeners_engine.*
import el.arn.checkers.helpers.EnumWithId

interface PreferencesManager : HoldsListeners<PreferencesManager.Listener> {
    fun createIntPref(key: String, possibleValues: Iterable<Int>?, defaultValue: Int): Preference<Int>
    fun createStringPref(key: String, possibleValues: Iterable<String>?, defaultValue: String): Preference<String>
    fun createBooleanPref(key: String, defaultValue: Boolean): Preference<Boolean>
    fun <E:EnumWithId> createEnumPref(key: String, possibleValues: Array<E>, defaultValue: E): Preference<E>

    interface Listener {
        /**called only when a pref's value is actually changed. assigning a pref with the same value invokes nothing*/
        fun prefsHaveChanged(changedPreference: Preference<*>)
    }

}

open class PreferencesManagerImpl(
    private val sharedPreferences: SharedPreferences,
    private val listenersMgr: ListenersManager<PreferencesManager.Listener> = ListenersManager()
): PreferencesManager, HoldsListeners<PreferencesManager.Listener> by listenersMgr {

    private val _prefs = mutableSetOf<Preference<*>>()

    override fun createIntPref(key: String, possibleValues: Iterable<Int>?, defaultValue: Int): Preference<Int> {
        val intPref = IntPreferenceImpl(key, possibleValues, defaultValue)
        registerPref(intPref)
        return intPref
    }
    override fun createStringPref(key: String, possibleValues: Iterable<String>?, defaultValue: String): Preference<String> {
        val stringPref = StringPreferenceImpl(key, possibleValues, defaultValue)
        registerPref(stringPref)
        return stringPref
    }
    override fun createBooleanPref(key: String, defaultValue: Boolean): Preference<Boolean> {
        val booleanPref = BooleanPreferenceImpl(key, defaultValue)
        registerPref(booleanPref)
        return booleanPref
    }
    override fun <E:EnumWithId> createEnumPref(key: String, possibleValues: Array<E>, defaultValue: E): Preference<E> {
        val enumPref = EnumPreferenceImpl(key, possibleValues, defaultValue)
        registerPref(enumPref)
        return enumPref
    }

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener {
            _: SharedPreferences, prefKey: String ->
        try {
            val changedPref = _prefs.first { it.key == prefKey }
            listenersMgr.notifyAll { it.prefsHaveChanged(changedPref) }
        } catch (e: NoSuchElementException) {
            //TODO log: sharedPrefLeak/unhandledPref
        }
    }

    private fun registerPref(preference: Preference<*>) {
        _prefs.add(preference)
        listenersMgr.addListener(object : PreferencesManager.Listener {
            override fun prefsHaveChanged(changedPreference: Preference<*>) {
                if (changedPreference == preference) {
                    (preference as PreferenceImpl).notifyListenersPrefHasChanged()
                }
            }
        })
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }


    private inner class IntPreferenceImpl (
        key: String,
        possibleValues: Iterable<Int>?,
        defaultValue: Int
    ): PreferenceImpl<Int>(key, possibleValues, defaultValue), Preference<Int> {

        override fun getValueFromSharedPreferences() = sharedPreferences.getInt(key, defaultValue)
        override fun writeValueToSharedPreferences(value: Int) = with(sharedPreferences.edit()) { putInt(key, value); apply() }

        init {
            initPreferenceValueIfKeyDoesNotExistInSharedPreferences(sharedPreferences)
        }
    }

    private inner class StringPreferenceImpl (
        key: String,
        possibleValues: Iterable<String>?,
        defaultValue: String
    ): PreferenceImpl<String>(key, possibleValues, defaultValue), Preference<String> {

        override fun getValueFromSharedPreferences() = sharedPreferences.getString(key, defaultValue)!!
        override fun writeValueToSharedPreferences(value: String) = with(sharedPreferences.edit()) { putString(key, value); apply() }

        init {
            initPreferenceValueIfKeyDoesNotExistInSharedPreferences(sharedPreferences)
        }
    }

    private inner class BooleanPreferenceImpl(
        key: String,
        defaultValue: Boolean
    ): PreferenceImpl<Boolean>(key, null, defaultValue), Preference<Boolean> {

        override fun getValueFromSharedPreferences() = sharedPreferences.getBoolean(key, defaultValue)
        override fun writeValueToSharedPreferences(value: Boolean) = with(sharedPreferences.edit()) { putBoolean(key, value); apply() }

        init {
            initPreferenceValueIfKeyDoesNotExistInSharedPreferences(sharedPreferences)
        }
    }

    private inner class EnumPreferenceImpl<E : EnumWithId> (
        key: String,
        possibleValues: Array<E>,
        defaultValue: E
    ): PreferenceImpl<E>(key, possibleValues.toList(), defaultValue), Preference<E> {

        override fun getValueFromSharedPreferences(): E = possibleValues!!.first { it.id == sharedPreferences.getString(key, defaultValue.id)!! }
        override fun writeValueToSharedPreferences(value: E) {
            with(sharedPreferences.edit()) { putString(key, value.id); apply() }
        }

        init {
            initPreferenceValueIfKeyDoesNotExistInSharedPreferences(sharedPreferences)
        }
    }

}
