import test.*

//var market = MutableMarket(2, 1.20.big(), 10000.big())
var market = MutableMarket(1.20.big(), 10000.big(), arrayOf(0.01.big(), 0.99.big()))

val product = market.pool(0).product()
println("product: $product")
println("sqrt(product): " + kotlin.math.sqrt(product.toDouble()))

market.worstCaseLoss(0)


//market.buy(2929.big())
//market.printPrices()
//market.arbitrage(2929.big())
//market.printPrices()

// TODO compute worst-case loss (with or without revenue)
// TODO test worst-case loss with different starting prices
// TODO implement alternative approximate arbitrage and compare to the exact method
//      this method targets the price ratio, e.g. 1.0/0.5 → 0.66/0.33
//      + mesures iterations of the exact method
