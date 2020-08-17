package el.arn.opencheckers.dialogs.composites

data class DialogButtonParams(val text: String, val doWhenClicked: (() -> Unit)?)