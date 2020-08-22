package el.arn.checkers.managers.preferences_managers

import android.content.SharedPreferences
import el.arn.checkers.helpers.listeners_engine.*
import el.arn.checkers.helpers.EnumWithId

open class PreferencesManager(
    private val sharedPreferences: SharedPreferences,
    private val listenersMgr: ListenersManager<Listener> = ListenersManager()
): HoldsListeners<PreferencesManager.Listener> by listenersMgr {

    private val _prefs = mutableSetOf<Pref<*>>()

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener {
            _: SharedPreferences, prefKey: String ->
        try {
            val changedPref = _prefs.first { it.key == prefKey }
            listenersMgr.notifyAll { it.prefsHaveChanged(changedPref) }
        } catch (e: NoSuchElementException) {
            //TODO log: sharedPrefLeak/unhandledPref
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }


    private fun registerPref(pref: Pref<*>) {
        _prefs.add(pref)
        listenersMgr.addListener(object : Listener {
            override fun prefsHaveChanged(changedPref: Pref<*>) {
                if (changedPref == pref) {
                    (pref as PrefImpl).notifyListenersPrefHasChanged()
                }
            }
        })

    }

    interface Listener {
        /**called only when a pref's value is actually changed. assigning a pref with the same value invokes nothing*/
        fun prefsHaveChanged(changedPref: Pref<*>)
    }


    fun createIntPref(key: String, possibleValues: Iterable<Int>?, defaultValue: Int): Pref<Int> {
        val intPref = IntPrefImpl(key, possibleValues, defaultValue)
        registerPref(intPref)
        return intPref
    }
    fun createStringPref(key: String, possibleValues: Iterable<String>?, defaultValue: String): Pref<String> {
        val stringPref = StringPrefImpl(key, possibleValues, defaultValue)
        registerPref(stringPref)
        return stringPref
    }
    fun createBooleanPref(key: String, defaultValue: Boolean): Pref<Boolean> {
        val booleanPref = BooleanPrefImpl(key, defaultValue)
        registerPref(booleanPref)
        return booleanPref
    }
    fun <E:EnumWithId> createEnumPref(key: String, possibleValues: Array<E>, defaultValue: E): Pref<E> {
        val enumPref = EnumPrefImpl(key, possibleValues, defaultValue)
        registerPref(enumPref)
        return enumPref
    }



    private inner class IntPrefImpl (
        key: String,
        possibleValues: Iterable<Int>?,
        defaultValue: Int
    ): PrefImpl<Int>(key, possibleValues, defaultValue), Pref<Int> {

        override fun getValueFromSharedPreferences() = sharedPreferences.getInt(key, defaultValue)
        override fun writeValueToSharedPreferences(value: Int) = with(sharedPreferences.edit()) { putInt(key, value); apply() }
    }

    private inner class StringPrefImpl (
        key: String,
        possibleValues: Iterable<String>?,
        defaultValue: String
    ): PrefImpl<String>(key, possibleValues, defaultValue), Pref<String> {

        override fun getValueFromSharedPreferences() = sharedPreferences.getString(key, defaultValue)!!
        override fun writeValueToSharedPreferences(value: String) = with(sharedPreferences.edit()) { putString(key, value); apply() }
    }

    private inner class BooleanPrefImpl(
        key: String,
        defaultValue: Boolean
    ): PrefImpl<Boolean>(key, null, defaultValue), Pref<Boolean> {

        override fun getValueFromSharedPreferences() = sharedPreferences.getBoolean(key, defaultValue)
        override fun writeValueToSharedPreferences(value: Boolean) = with(sharedPreferences.edit()) { putBoolean(key, value); apply() }
    }


    private inner class EnumPrefImpl<E : EnumWithId> (
        key: String,
        possibleValues: Array<E>,
        defaultValue: E
    ): PrefImpl<E>(key, possibleValues.toList(), defaultValue), Pref<E> {

        override fun getValueFromSharedPreferences(): E {
            val a = possibleValues!!.first { it.id == sharedPreferences.getString(key, defaultValue.id)!! }
            return a
        }
        override fun writeValueToSharedPreferences(value: E) {
            with(sharedPreferences.edit()) { putString(key, value.id); apply() }
            val a = getValueFromSharedPreferences()
            val b = 1
        }
    }

}

interface Pref<V> : HoldsListeners<Pref.Listener<V>> {
    val key: String
    val possibleValues: Iterable<V>?
    val defaultValue: V
    var value: V
    fun restoreToDefault()

    interface Listener<V> {
        /**called only when the pref's value is actually changed. assigning the pref with the same value invokes nothing*/
        fun prefHasChanged(pref: Pref<V>, value: V)
    }
}

abstract class PrefImpl<V> (
    override val key: String,
    override val possibleValues: Iterable<V>?,
    final override val defaultValue: V,
    protected val preflistenersMgr: ListenersManager<Pref.Listener<V>> = ListenersManager()
): Pref<V>, HoldsListeners<Pref.Listener<V>> by preflistenersMgr {

    override fun restoreToDefault() {
        value = defaultValue
    }
    fun notifyListenersPrefHasChanged() {
        preflistenersMgr.notifyAll { it.prefHasChanged(this, value) }
    }

    override var value: V
        get() =
            getValueFromSharedPreferences()
        set(v) {
            assertValueIsInRange(v)
            if (this.value != v) {
                writeValueToSharedPreferences(v)
            }
        }
    private fun assertValueIsInRange(value: V) {
        if (possibleValues != null && value !in possibleValues ?: emptyList()) {
            throw InternalError(value.toString() + " is not a possible value for pref " + key)
        }
    }
    init {
        assertValueIsInRange(defaultValue)
    }
    protected abstract fun getValueFromSharedPreferences(): V
    protected abstract fun writeValueToSharedPreferences(value: V)

}
