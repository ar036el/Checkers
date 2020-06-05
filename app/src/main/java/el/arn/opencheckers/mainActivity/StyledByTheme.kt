package el.arn.opencheckers.mainActivity

import el.arn.opencheckers.R
import el.arn.opencheckers.delegationMangement.LimitedDelegate
import el.arn.opencheckers.delegationMangement.LimitedDelegateImpl
import el.arn.opencheckers.prefs_managers.MainPrefsManager
import el.arn.opencheckers.prefs_managers.Pref

interface StyledByTheme {
    val themedResource: ThemedResource<*>
    fun enableAutoRefresh(
        updateStyleFunc: (ThemedResource<*>) -> Unit,
        stopWhen: () -> Boolean)
    fun disableAutoRefresh()
}

class StyledByThemeImpl<T>(
    override val themedResource: ThemedResource<T>
) : StyledByTheme {

    private var prefDelegate: Pref.Delegate<T>? = null

    override fun enableAutoRefresh(
        updateStyleFunc: (ThemedResource<*>) -> Unit,
        stopWhen: () -> Boolean) {

        val _prefDelegate = object : Pref.Delegate<T> , LimitedDelegate by LimitedDelegateImpl(destroyIf = stopWhen) {
            override fun prefHasChanged(pref: Pref<T>, value: T) {
                updateStyleFunc(themedResource)
            }
        }
        prefDelegate = _prefDelegate
        themedResource.pref.addDelegate(_prefDelegate)
    }
    override fun disableAutoRefresh() {
        prefDelegate?.let { themedResource.pref.removeDelegate(it) }
    }
}

object ThemedResources {
    object Drawables {
        private val prefsManager = MainPrefsManager() //TODO not here

        val unplayableTile = ThemedResource(
            prefsManager.boardTheme,
            1 to R.color.tile_notPlayable_1,
            2 to R.color.tile_notPlayable_2,
            3 to R.color.tile_notPlayable_3,
            4 to R.color.tile_notPlayable_4
        )

        val playableTile = ThemedResource(
            prefsManager.boardTheme,
            1 to R.color.tile_playable_1,
            2 to R.color.tile_playable_2,
            3 to R.color.tile_playable_3,
            4 to R.color.tile_playable_4
        )

        val whitePawn = ThemedResource(
            prefsManager.piecesTheme,
            1 to R.drawable.piece_white_pawn_1,
            2 to R.drawable.piece_white_pawn_2,
            3 to R.drawable.piece_white_pawn_3
        )

        val whiteKing = ThemedResource(
            prefsManager.piecesTheme,
            1 to R.drawable.piece_white_king_1,
            2 to R.drawable.piece_white_king_2,
            3 to R.drawable.piece_white_king_3
        )

        val blackPawn = ThemedResource(
            prefsManager.piecesTheme,
            1 to R.drawable.piece_black_pawn_1,
            2 to R.drawable.piece_black_pawn_2,
            3 to R.drawable.piece_black_pawn_3
        )

        val blackKing = ThemedResource(
            prefsManager.piecesTheme,
            1 to R.drawable.piece_black_king_1,
            2 to R.drawable.piece_black_king_2,
            3 to R.drawable.piece_black_king_3
        )
    }

}


class ThemedResource<V>(
    val pref: Pref<V>,
    vararg prefValuesToResources: Pair<V, Int>
) {
    private val resources = mapOf(*prefValuesToResources)
    fun getResource() = resources[pref.value]!!
    init {
        if (pref.possibleValues == null) {
            throw InternalError("pref.possibleValues must be non-null")
        }
        if (resources.keys != pref.possibleValues) {
            throw InternalError("map must match pref.possibleValues perfectly")
        }
    }
}