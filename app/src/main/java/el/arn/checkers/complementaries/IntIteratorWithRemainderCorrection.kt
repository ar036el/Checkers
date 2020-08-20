package el.arn.checkers.complementaries

class IntIteratorWithRemainderCorrection(private val const: Double) {
    private var remainder: Double = 0.0
    fun getInt(): Int {
        remainder += const
        val result = remainder.toInt()
        remainder -= result.toDouble()
        return result
    }
}