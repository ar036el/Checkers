package el.arn.opencheckers.dialogs

interface Dialog {
    abstract val isShowing: Boolean
    abstract fun dismiss()
}