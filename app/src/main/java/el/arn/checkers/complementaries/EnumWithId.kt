package el.arn.checkers.complementaries

/** Used for enum classes that are being consumed for some registry. [id] will be used instead of [toString], so no problems will be caused if we want to refactor the enum's names
 */
interface EnumWithId {
    val id: String
}