package el.arn.checkers.dialogs

interface Dialog {
    abstract val isShowing: Boolean
    abstract fun dismiss()
}