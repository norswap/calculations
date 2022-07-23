package test
import java.math.BigDecimal as Big

fun bundlePrice (pools: List<ImmutablePool>): Big =
    pools.map(ImmutablePool::price).reduce(Big::add)

interface Market<Self: Market<Self>> {
    fun arbThreshold(): Big
    fun revenue(): Big
    fun pools(): List<ImmutablePool>
    fun pool(index: Int) = pools()[index]

    fun buyPrice  (index: Int, amount: Big) = pools()[index].buyPrice(amount)
    fun sellPrice (index: Int, amount: Big) = pools()[index].sellPrice(amount)
    fun bundlePrice(): Big = bundlePrice(pools())

    fun buy (index: Int, amount: Big): Self
    fun sell (index: Int, amount: Big): Self
    fun arbitrage (amount: Big): Self
    fun worstCaseLoss (index: Int): Big

    fun poolsValue(): Big {
        return pools().map{ it.value() }.reduce { acc, v -> acc + v }.round(18)
    }

    fun lpValue(): Big {
        return (poolsValue() + revenue()).round(18)
    }

    fun printPrices() {
        pools().forEachIndexed { i, pool -> println("pool $i: ${pool.price().round(18)}") }
        println("bundle price: ${bundlePrice().round(18)}")
        println("revenue: ${revenue().round(18)}")
        println("pool values: ${poolsValue()}")
        println("LP value: ${lpValue()}")
    }
}

data class ImmutableMarket (val revenue: Big, val arbThreshold: Big, val pools: List<ImmutablePool>): Market<ImmutableMarket> {
    override fun revenue() = revenue
    override fun arbThreshold() = arbThreshold
    override fun pools() = pools

    constructor(npools: Int, arbThreshold: Big, initialLiquidity: Big): this(
        Big.ZERO,
        arbThreshold,
        List(npools) { ImmutablePool(initialLiquidity, initialLiquidity / npools.big()) })

    constructor(arbThreshold: Big, initialLiquidity: Big, initialWeights: Array<Big>): this(
        Big.ZERO,
        arbThreshold,
        initialWeights.map { ImmutablePool(initialLiquidity, it * initialLiquidity) })

    override fun buy (index: Int, amount: Big): ImmutableMarket {
        val pool = pools[index].buy(amount)
        val pools = ArrayList(this.pools)
        pools[index] = pool
        return ImmutableMarket(revenue, arbThreshold, pools)
    }

    override fun sell (index: Int, amount: Big): ImmutableMarket {
        val pool = pools[index].sell(amount)
        val pools = ArrayList(this.pools)
        pools[index] = pool
        return ImmutableMarket(revenue, arbThreshold, pools)
    }

    override fun worstCaseLoss (index: Int): Big {
        var market = this
        var volume = Big.ZERO
        var i = 0
        while (true) {
            val maxBuyable = market.pool(index).maxBuyable()
            val maxToArbitrageThreshold = market.pool(index).buyAmountToReachPrice(arbThreshold)
            val buyAmount = maxBuyable.min(maxToArbitrageThreshold);
            println(buyAmount.round(18))
            if (buyAmount < Big.ONE) break
            volume += buyAmount
            println("volume: ${volume.round(18)}")
            market = market.buy(index, buyAmount).arbitrage(buyAmount)
            market.printPrices()
            i += 1
        }
        println(i)
        market.printPrices()
        return Big.ZERO
    }

    override fun arbitrage (amount: Big): ImmutableMarket {
        var min = Big.ZERO
        var max = amount
        while (true) {
            require(min < max)
            val mid = min + ((max - min) / 2.big())
            var arbRevenue = Big.ZERO
            val pools = this.pools.map {
                arbRevenue += it.sellPrice(mid) * mid
                it.sell(mid)
            }
            val sum = bundlePrice(pools)
            if (sum > Big.ONE) min = mid
            else max = mid

            if (min >= max || sum.cmpDigits(Big.ONE, 3) == 0) { // sum == 1
                return ImmutableMarket(revenue + arbRevenue - mid, arbThreshold, pools)
            }
        }
    }
}

class MutableMarket (var market: ImmutableMarket): Market<MutableMarket> {
    override fun revenue() = market.revenue
    override fun pools() = market.pools
    override fun arbThreshold() = market.arbThreshold

    constructor(npools: Int, arbThreshold: Big, initialLiquidity: Big):
        this(ImmutableMarket(npools, arbThreshold, initialLiquidity))

    constructor(arbThreshold: Big, initialLiquidity: Big, initialWeights: Array<Big>):
        this(ImmutableMarket(arbThreshold, initialLiquidity, initialWeights))

    override fun buy (index: Int, amount: Big): MutableMarket {
        market = market.buy(index, amount)
        return this
    }

    override fun sell (index: Int, amount: Big): MutableMarket {
        market = market.sell(index, amount)
        return this
    }

    override fun worstCaseLoss (index: Int): Big {
        return market.worstCaseLoss(index)
    }

    override fun arbitrage(amount: Big): MutableMarket {
        market = market.arbitrage(amount)
        return this
    }
}


