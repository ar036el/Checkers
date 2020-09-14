package el.arn.checkers.dialogs

interface Dialog {
    val isShowing: Boolean
    fun dismiss()
}