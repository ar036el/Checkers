package el.arn.checkers.helpers.selections

abstract class OutOf {
    abstract val selected: Int
}

class OutOf2(override val selected: Int) : OutOf() {
    init {
        if (selected < 0 || selected > 1) {
            throw InternalError("OutOf selection out of bound")
        }
    }

    fun isFirst() = selected == 0
    fun isSecond() = selected == 1
}

class OutOf3(override val selected: Int) : OutOf() {
    init {
        if (selected < 0 || selected > 2) {
            throw InternalError("OutOf selection out of bound")
        }
    }

    fun isFirst() = selected == 0
    fun isSecond() = selected == 1
    fun isThird() = selected == 2
}

class OutOf4(override val selected: Int) : OutOf() {
    init {
        if (selected < 0 || selected > 3) {
            throw InternalError("OutOf selection out of bound")
        }
    }

    fun isFirst() = selected == 0
    fun isSecond() = selected == 1
    fun isThird() = selected == 2
    fun isFourth() = selected == 3
}