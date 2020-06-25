package el.arn.opencheckers.dialogs.parts

data class DialogButtonParams(val text: String, val doWhenClicked: (() -> Unit)?)