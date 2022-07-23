package test

import java.util.*

fun require (cond: Boolean) {
    if (!cond) throw AssertionError()
}

fun require (cond: Boolean, msg: () -> Any) {
    if (!cond) throw AssertionError(msg())
}

fun require (cond: Boolean, msg: Any) {
    if (!cond) throw AssertionError(msg)
}

fun <T> requireEquals(x: T, y: T) {
    if (!Objects.equals(x, y))
        throw AssertionError("not equals: [$x] and [$y]")
}