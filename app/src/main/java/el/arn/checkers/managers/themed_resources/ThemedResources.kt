package el.arn.checkers.managers.themed_resources

import el.arn.checkers.R
import el.arn.checkers.appRoot
import el.arn.checkers.managers.preferences_managers.Preference

object ThemedResources {

    object Drawables {
        val unplayableTile =
            ThemedResource(
                appRoot.gamePreferencesManager.boardTheme,
                0 to R.color.tile_notPlayable_0,
                1 to R.color.tile_notPlayable_1,
                2 to R.color.tile_notPlayable_2,
                3 to R.color.tile_notPlayable_3,
                4 to R.color.tile_notPlayable_4
            )

        val playableTile =
            ThemedResource(
                appRoot.gamePreferencesManager.boardTheme,
                0 to R.color.tile_playable_0,
                1 to R.color.tile_playable_1,
                2 to R.color.tile_playable_2,
                3 to R.color.tile_playable_3,
                4 to R.color.tile_playable_4
            )

        val whitePawn =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_white_pawn_0,
                1 to R.drawable.piece_white_pawn_1,
                2 to R.drawable.piece_white_pawn_2,
                3 to R.drawable.piece_white_pawn_3,
                4 to R.drawable.piece_white_pawn_4,
                5 to R.drawable.piece_white_pawn_5,
                6 to R.drawable.piece_white_pawn_6,
                7 to R.drawable.piece_white_pawn_7,
                8 to R.drawable.piece_white_pawn_8,
                9 to R.drawable.piece_white_pawn_9
            )

        val whiteKing =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_white_king_0,
                1 to R.drawable.piece_white_king_1,
                2 to R.drawable.piece_white_king_2,
                3 to R.drawable.piece_white_king_3,
                4 to R.drawable.piece_white_king_4,
                5 to R.drawable.piece_white_king_5,
                6 to R.drawable.piece_white_king_6,
                7 to R.drawable.piece_white_king_7,
                8 to R.drawable.piece_white_king_8,
                9 to R.drawable.piece_white_king_9
            )

        val blackPawn =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_black_pawn_0,
                1 to R.drawable.piece_black_pawn_1,
                2 to R.drawable.piece_black_pawn_2,
                3 to R.drawable.piece_black_pawn_3,
                4 to R.drawable.piece_black_pawn_4,
                5 to R.drawable.piece_black_pawn_5,
                6 to R.drawable.piece_black_pawn_6,
                7 to R.drawable.piece_black_pawn_7,
                8 to R.drawable.piece_black_pawn_8,
                9 to R.drawable.piece_black_pawn_9
            )

        val blackKing =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_black_king_0,
                1 to R.drawable.piece_black_king_1,
                2 to R.drawable.piece_black_king_2,
                3 to R.drawable.piece_black_king_3,
                4 to R.drawable.piece_black_king_4,
                5 to R.drawable.piece_black_king_5,
                6 to R.drawable.piece_black_king_6,
                7 to R.drawable.piece_black_king_7,
                8 to R.drawable.piece_black_king_8,
                9 to R.drawable.piece_black_king_9
            )

        val mixedPawn =
            ThemedResource(
                appRoot.gamePreferencesManager.playersTheme,
                0 to R.drawable.piece_both_players_0,
                1 to R.drawable.piece_both_players_1,
                2 to R.drawable.piece_both_players_2,
                3 to R.drawable.piece_both_players_3,
                4 to R.drawable.piece_both_players_4,
                5 to R.drawable.piece_both_players_5,
                6 to R.drawable.piece_both_players_6,
                7 to R.drawable.piece_both_players_7,
                8 to R.drawable.piece_both_players_8,
                9 to R.drawable.piece_both_players_9
            )
    }

    object Raws {
        val soundEffectPieceCaptured =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_captured_0,
                2 to R.raw.soundeffect_captured_1,
                3 to R.raw.soundeffect_captured_2
            )

        val soundEffectPieceMovedPlayer1 =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_moved_player1_0,
                2 to R.raw.soundeffect_moved_player1_1,
                3 to null
            )

        val soundEffectPieceMovedPlayer2 =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_moved_player2_0,
                2 to R.raw.soundeffect_moved_player2_1,
                3 to null
            )

        val soundEffectPieceTurnedIntoKing =
            ThemedResourceNullable(
                appRoot.gamePreferencesManager.soundEffectsTheme,
                0 to null,
                1 to R.raw.soundeffect_king_0,
                2 to R.raw.soundeffect_king_1,
                3 to R.raw.soundeffect_king_2
            )

    }

}

open class ThemedResourceNullable<V>(
    val preference: Preference<V>,
    vararg allPrefPossibleValuesWithItsCorrespondingIntResources: Pair<V, Int?>
) {
    private val resources = mapOf(*allPrefPossibleValuesWithItsCorrespondingIntResources)
    open fun getResource(): Int? = resources[preference.value]
    init {
        if (preference.possibleValues == null) {
            throw InternalError("pref.possibleValues must be non-null")
        }
        if (resources.keys != preference.possibleValues?.toSet()) {
            throw InternalError("map must match pref.possibleValues perfectly")
        }
    }
}

class ThemedResource<V>(
    preference: Preference<V>,
    vararg prefValuesToResources: Pair<V, Int>
) : ThemedResourceNullable<V>(preference, *prefValuesToResources){
    override fun getResource(): Int {
        return super.getResource()!!
    }
}