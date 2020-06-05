package el.arn.opencheckers.helpers

fun max(one: Int, other: Int) = one.coerceAtLeast(other)
fun min(one: Int, other: Int) = one.coerceAtMost(other)