package test
import java.math.MathContext
import java.math.BigDecimal as Big

interface IPool<Self: IPool<Self>> {
    fun answer(): Big
    fun stable(): Big

    fun price() = stable() / answer()
    fun product() = answer() * stable()
    fun value() = stable() * 2.big()

    fun buy (amount: Big): Self
    fun sell (amount: Big): Self

    fun buyPrice (amount: Big): Big {
        val answer1 = answer() - amount
        val stable1 = product() / answer1
        return (stable1 - stable()) / amount
    }

    fun sellPrice (amount: Big): Big {
        val answer1 = answer() + amount
        val stable1 = product() / answer1
        return (stable() - stable1) / amount
    }

    fun buyAmountToReachPrice (price: Big): Big {
        if (price <= price())
            throw IllegalArgumentException("target price lower than current price")

        // Computation details:
        // - assume x is answer, y is stable
        // x1 = x0 * fx
        // y1 = y0 / fx
        // p1 = y1 / x1 = (y0 / fx) / (x0 * fx) = p0 / fx^2
        // fx = sqrt(p0 / p1)

        val fx = (price() / price).sqrt(MathContext.DECIMAL128)
        assert(fx < Big.ONE)
        return (Big.ONE - fx) * answer()
    }

    fun sellAmountToReachPrice (price: Big): Big {
        if (price >= price())
            throw IllegalArgumentException("target price higher than current price")

        // Computation details:
        // - assume x is answer, y is stable
        // x1 = x0 * fx
        // y1 = y0 / fx
        // p1 = y1 / x1 = (y0 / fx) / (x0 * fx) = p0 / fx^2
        // fx = sqrt(p0 / p1)

        val fx = (price() / price).sqrt(MathContext.DECIMAL128)
        assert(fx > Big.ONE)
        return (fx - Big.ONE) * answer()
    }

    fun maxBuyable(): Big {
        return buyAmountToReachPrice(Big.ONE)
    }

    fun maxSellable (minPrice: Big): Big {
        return sellAmountToReachPrice(minPrice)
    }
}

data class ImmutablePool (var answer: Big, var stable: Big): IPool<ImmutablePool> {
    override fun answer() = answer
    override fun stable() = stable

    // Buys the given amount of answer tokens and return the unit price.
    fun buyMutate (amount: Big): Big {
        val oldStable = stable
        val oldProduct = product()
        answer -= amount
        stable = oldProduct / answer
        return (stable - oldStable) / amount
    }

    // Buys the given amount of answer tokens and return the unit price.
    fun sellMutate (amount: Big): Big {
        val oldStable = stable
        val oldProduct = product()
        answer += amount
        stable = oldProduct / answer
        return (oldStable - stable) / amount
    }

    // Buys the given amount of answer tokens and return the new pool
    override fun buy (amount: Big): ImmutablePool {
        val answer1 = answer - amount
        return ImmutablePool(answer1, product() / answer1)
    }

    // Sells the given amount of answer tokens and return the new pool
    override fun sell (amount: Big): ImmutablePool {
        val answer1 = answer + amount
        return ImmutablePool(answer1, product() / answer1)
    }
}

abstract class AbstractPool<Self: IPool<Self>>: IPool<Self>, Cloneable {
    fun component1() = answer()
    fun component2() = stable()

    override fun equals (other: Any?): Boolean {
        if (this === other) return true
        if (other !is IPool<*>) return false;
        return other.answer() == answer() && other.stable() == stable()
    }

    override fun hashCode(): Int {
        return answer().hashCode() * 31 + stable().hashCode();
    }

    override fun toString() = ImmutablePool(answer(), stable()).toString()
}

class MutablePool (var pool: ImmutablePool): AbstractPool<MutablePool>() {
    override fun answer() = pool.answer
    override fun stable() = pool.stable

    override fun buy(amount: Big): MutablePool {
        pool = pool.buy(amount)
        return this
    }

    override fun sell(amount: Big): MutablePool {
        pool = pool.sell(amount)
        return this
    }

    override fun clone() = MutablePool(pool)
}