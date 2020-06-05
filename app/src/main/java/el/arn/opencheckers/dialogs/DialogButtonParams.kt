package el.arn.opencheckers.dialogs

data class DialogButtonParams(val text: String, val doWhenClicked: (() -> Unit)?)