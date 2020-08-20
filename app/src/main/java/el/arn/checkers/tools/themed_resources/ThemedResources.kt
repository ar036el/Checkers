package el.arn.checkers.tools.themed_resources

import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.tools.preferences_managers.Pref

object ThemedResources {

    object Drawables {
        val unplayableTile =
            ThemedResource(
                appRoot.gamePreferencesManager.boardTheme,
                0 to R.color.tile_notPlayable_1,
                1 to R.color.tile_notPlayable_2,
                2 to R.color.tile_notPlayable_3,
                3 to R.color.tile_notPlayable_4
            )

        val playableTile =
            ThemedResource(
                appRoot.gamePreferencesManager.boardTheme,
                0 to R.color.tile_playable_1,
                1 to R.color.tile_playable_2,
                2 to R.color.tile_playable_3,
                3 to R.color.tile_playable_4
            )

        val whitePawn =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_white_pawn_1,
                1 to R.drawable.piece_white_pawn_2,
                2 to R.drawable.piece_white_pawn_3
            )

        val whiteKing =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_white_king_1,
                1 to R.drawable.piece_white_king_2,
                2 to R.drawable.piece_white_king_3
            )

        val blackPawn =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_black_pawn_1,
                1 to R.drawable.piece_black_pawn_2,
                2 to R.drawable.piece_black_pawn_3
            )

        val blackKing =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_black_king_1,
                1 to R.drawable.piece_black_king_2,
                2 to R.drawable.piece_black_king_3
            )

        val mixedPawn =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_both_players_1,
                1 to R.drawable.piece_both_players_2,
                2 to R.drawable.piece_both_players_3
            )
    }

    object Raws {
        val soundEffectPieceCaptured =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_captured_1,
                2 to R.raw.soundeffect_captured_2,
                3 to R.raw.soundeffect_captured_3
            )

        val soundEffectPieceMovedPlayer1 =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_moved_player1_1,
                2 to R.raw.soundeffect_moved_player1_2,
                3 to null
            )

        val soundEffectPieceMovedPlayer2 =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_moved_player2_1,
                2 to R.raw.soundeffect_moved_player2_2,
                3 to null
            )

        val soundEffectPieceTurnedIntoKing =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_king_1,
                2 to R.raw.soundeffect_king_2,
                3 to R.raw.soundeffect_king_3
            )

    }

}

open class ThemedResourceNullable<V>(
    val pref: Pref<V>,
    vararg allPrefPossibleValuesWithItsCorrespondingIntResources: Pair<V, Int?>
) {
    private val resources = mapOf(*allPrefPossibleValuesWithItsCorrespondingIntResources)
    open fun getResource(): Int? = resources[pref.value]
    init {
        if (pref.possibleValues == null) {
            throw InternalError("pref.possibleValues must be non-null")
        }
        if (resources.keys != pref.possibleValues?.toSet()) {
            throw InternalError("map must match pref.possibleValues perfectly")
        }
    }
}

class ThemedResource<V>(
    pref: Pref<V>,
    vararg prefValuesToResources: Pair<V, Int>
) : ThemedResourceNullable<V>(pref, *prefValuesToResources){
    override fun getResource(): Int {
        return super.getResource()!!
    }
}