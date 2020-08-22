package el.arn.checkers.managers.themed_resources

import el.arn.checkers.helpers.listeners_engine.LimitedListener
import el.arn.checkers.helpers.listeners_engine.LimitedListenerImpl
import el.arn.checkers.managers.preferences_managers.Pref

interface ChangesStyleByTheme {
    val themedResource: ThemedResource<*>
    fun enableAutoRefresh(
        updateStyleFunc: (ThemedResource<*>) -> Unit,
        stopWhen: () -> Boolean)
    fun disableAutoRefresh()
}

class ChangesStyleByTheme_implByDelegation<T>(
    override val themedResource: ThemedResource<T>
) : ChangesStyleByTheme {

    private var prefListener: Pref.Listener<T>? = null

    override fun enableAutoRefresh(
        updateStyleFunc: (ThemedResource<*>) -> Unit,
        stopWhen: () -> Boolean) {

        val _prefListener = object : Pref.Listener<T> , LimitedListener by LimitedListenerImpl(destroyIf = stopWhen) {
            override fun prefHasChanged(pref: Pref<T>, value: T) {
                updateStyleFunc(themedResource)
            }
        }
        prefListener = _prefListener
        themedResource.pref.addListener(_prefListener)
    }
    override fun disableAutoRefresh() {
        prefListener?.let { themedResource.pref.removeListener(it) }
    }
}