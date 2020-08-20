package el.arn.checkers.dialogs.composites

data class DialogButtonParams(val text: String, val doWhenClicked: (() -> Unit)?)