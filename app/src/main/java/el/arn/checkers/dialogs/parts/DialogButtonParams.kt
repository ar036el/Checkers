package el.arn.checkers.dialogs.parts

data class DialogButtonParams(val text: String, val doWhenClicked: (() -> Unit)?)