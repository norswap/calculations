package test
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

val BIGCTX = MathContext(18, RoundingMode.HALF_EVEN)

fun Int.big(): BigDecimal {
    return BigDecimal(this, BIGCTX)
}

fun Long.big(): BigDecimal {
    return BigDecimal(this, BIGCTX)
}

fun Double.big(): BigDecimal {
    return BigDecimal(this, BIGCTX)
}

fun BigDecimal.round (digits: Int): BigDecimal {
    return this.setScale(digits, RoundingMode.HALF_EVEN)
}

fun BigDecimal.cmpDigits (other: BigDecimal, digits: Int): Int {
    return this.round(digits).compareTo(other.round(digits));
}

operator fun BigDecimal.plus(other: BigDecimal): BigDecimal {
    return this.add(other, BIGCTX)
}

operator fun BigDecimal.minus(other: BigDecimal): BigDecimal {
    return this.subtract(other, BIGCTX)
}

operator fun BigDecimal.times(other: BigDecimal): BigDecimal {
    return this.multiply(other, BIGCTX)
}

operator fun BigDecimal.div(other: BigDecimal): BigDecimal {
    return this.divide(other, BIGCTX)
}

operator fun BigDecimal.rem(other: BigDecimal): BigDecimal {
    return this.remainder(other, BIGCTX)
}