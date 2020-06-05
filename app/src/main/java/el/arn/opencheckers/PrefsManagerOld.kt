package el.arn.opencheckers

import android.content.SharedPreferences



abstract class PrefsManagerOLDO(
    private val sharedPreferences: SharedPreferences
) {

    abstract inner class Pref<V>(
        val key: String,
        val possibleValues: Iterable<V>?,
        val defaultValue: V
    ) {
        abstract var value: V
        fun restoreToDefault() {
            value = defaultValue
        }
        protected fun assertIsInRange(value: V) {
            if (possibleValues != null && value !in possibleValues) {
                throw InternalError("not a possible value")
            }
        }
        init {
            assertIsInRange(defaultValue)
        }
    }

    inner class IntPref(
        key: String,
        possibleValues: Iterable<Int>?,
        defaultValue: Int
    ): Pref<Int>(key, possibleValues, defaultValue) {
        override var value: Int
            get() = sharedPreferences.getInt(key, defaultValue)
            set(value) {
                assertIsInRange(value)
                with (sharedPrefs.edit()) { putInt(key, value); apply() }
            }
    }
    inner class StringPref(
        key: String,
        possibleValues: Iterable<String>?,
        defaultValue: String
    ): Pref<String>(key, possibleValues, defaultValue) {
        override var value: String
            get() = sharedPreferences.getString(key, defaultValue)!!
            set(value) {
                assertIsInRange(value)
                with (sharedPrefs.edit()) { putString(key, value); apply() }
            }
    }

}


object Prefs {
    val kingBehaviour = StringPref(
        StringsRes.get(R.string.pref_kingBehaviour),
        StringsRes.get(R.string.pref_kingBehaviour_defaultValue))

    val canManCaptureBackwards = StringPref(
        StringsRes.get(R.string.pref_canManCaptureBackwards),
        StringsRes.get(R.string.pref_canManCaptureBackwards_defaultValue))

    val isCapturingMandatory = BooleanPref(
        StringsRes.get(R.string.pref_isCapturingMandatory),
        Booleans.get(R.bool.pref_isCapturingMandatory_defaultValue))

    val boardTheme = IntPref(
        StringsRes.get(R.string.pref_boardTheme),
        Integers.get(R.integer.pref_boardTheme_defaultValue))

    val playerTheme = IntPref(
        StringsRes.get(R.string.pref_playerTheme),
        Integers.get(R.integer.pref_playerTheme_defaultValue))

    val boardSize = StringPref(
        StringsRes.get(R.string.pref_boardSize),
        StringsRes.get(R.string.pref_boardSize_defaultValue))

    val isCustomSettingsEnabled = BooleanPref(
        StringsRes.get(R.string.pref_customSettingsEnabled),
        Booleans.get(R.bool.pref_customSettingsEnabled_defaultValue))

    val customStartingRows = StringPref(
        StringsRes.get(R.string.pref_customStartingRows),
        StringsRes.get(R.string.pref_customStartingRows_defaultValue))

    val customBoardSize = StringPref(
        StringsRes.get(R.string.pref_customBoardSize),
        StringsRes.get(R.string.pref_customBoardSize_defaultValue))

}





class StringPref(val key: String, val defaultValue: String)
class IntPref(val key: String, val defaultValue: Int)
class LongPref(val key: String, val defaultValue: Long)
class FloatPref(val key: String, val defaultValue: Float)
class BooleanPref(val key: String, val defaultValue: Boolean)

fun SharedPreferences.get(stringPref: StringPref): String =
    this.getString(stringPref.key, stringPref.defaultValue)!!

fun SharedPreferences.get(intPref: IntPref): Int =
    this.getInt(intPref.key, intPref.defaultValue)

fun SharedPreferences.get(longPref: LongPref): Long =
    this.getLong(longPref.key, longPref.defaultValue)

fun SharedPreferences.get(floatPref: FloatPref): Float =
    this.getFloat(floatPref.key, floatPref.defaultValue)

fun SharedPreferences.get(booleanPref: BooleanPref): Boolean =
    this.getBoolean(booleanPref.key, booleanPref.defaultValue)


class StringEnumConverter<E> {

    private val stringToEnum: Map<String, E>
    private val enumToString: Map<E, String>

    constructor(vararg entries: Pair<String,E>) {
        stringToEnum = mapOf(*entries)
        enumToString = mapOf(*entries.map { Pair(it.second, it.first) }.toTypedArray())
    }

    constructor(enumValues: Collection<E>, vararg strings: String) {
        if (enumValues.size != strings.size) throw InternalError("must be same size")
        stringToEnum = mapOf(*strings.zip(enumValues).toTypedArray())
        enumToString = mapOf(*enumValues.zip(strings).toTypedArray())
    }

    fun getEnum(string: String) = stringToEnum[string] ?: error("no item found")
    fun getString(enumValue: E) = enumToString[enumValue] ?: error("no item found")

}