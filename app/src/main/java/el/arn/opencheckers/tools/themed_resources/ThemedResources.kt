package el.arn.opencheckers.tools.themed_resources

import el.arn.opencheckers.R
import el.arn.opencheckers.tools.preferences_managers.GamePreferencesManager
import el.arn.opencheckers.tools.preferences_managers.Pref

object ThemedResources {
    object Drawables {
        private val prefsManager = GamePreferencesManager() //TODO not here

        val unplayableTile =
            ThemedResource(
                prefsManager.boardTheme,
                1 to R.color.tile_notPlayable_1,
                2 to R.color.tile_notPlayable_2,
                3 to R.color.tile_notPlayable_3,
                4 to R.color.tile_notPlayable_4
            )

        val playableTile =
            ThemedResource(
                prefsManager.boardTheme,
                1 to R.color.tile_playable_1,
                2 to R.color.tile_playable_2,
                3 to R.color.tile_playable_3,
                4 to R.color.tile_playable_4
            )

        val whitePawn =
            ThemedResource(
                prefsManager.piecesTheme,
                1 to R.drawable.piece_white_pawn_1,
                2 to R.drawable.piece_white_pawn_2,
                3 to R.drawable.piece_white_pawn_3
            )

        val whiteKing =
            ThemedResource(
                prefsManager.piecesTheme,
                1 to R.drawable.piece_white_king_1,
                2 to R.drawable.piece_white_king_2,
                3 to R.drawable.piece_white_king_3
            )

        val blackPawn =
            ThemedResource(
                prefsManager.piecesTheme,
                1 to R.drawable.piece_black_pawn_1,
                2 to R.drawable.piece_black_pawn_2,
                3 to R.drawable.piece_black_pawn_3
            )

        val blackKing =
            ThemedResource(
                prefsManager.piecesTheme,
                1 to R.drawable.piece_black_king_1,
                2 to R.drawable.piece_black_king_2,
                3 to R.drawable.piece_black_king_3
            )

        val mixedPawn =
            ThemedResource(
                prefsManager.piecesTheme,
                1 to R.drawable.piece_both_players_1,
                2 to R.drawable.piece_both_players_2,
                3 to R.drawable.piece_both_players_3
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
        if (resources.keys != pref.possibleValues?.toSet()) {
            throw InternalError("map must match pref.possibleValues perfectly")
        }
    }
}