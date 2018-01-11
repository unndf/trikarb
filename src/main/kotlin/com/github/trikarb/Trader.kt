package com.github.trikarb

import java.math.BigDecimal
import java.math.MathContext
import com.github.ccob.bittrex4j.dao.UpdateExchangeState
import com.github.ccob.bittrex4j.dao.MarketOrdersResult

class Trader(val markets: List<String>, val base: String, val maxCycleLength: Int=4){
    
    val orderbooks: Map<String, Orderbook> = markets.map{name ->
        val base = name.split("-")[0]
        val quote = name.split("-")[1]
        name to Orderbook(base,quote)
    }.toMap()
    
    val edges: List<DirectedEdge> = orderbooks.values.toList().asEdgeList()
    val cycles: List<ArbitrageCycle> = (3..maxCycleLength).flatMap{length -> edges.getArbitrageCycles(base,length=length)}
 
    public fun updateOrderbook(name: String, state: UpdateExchangeState){
        val orderbook = orderbooks[name]

        if (orderbook != null) {
            state.buys.forEach{ bid -> 
                when (bid.type) {
                    0 -> orderbook.addBid(bid.rate,bid.quantity)
                    1 -> orderbook.removeBid(bid.rate)
                    2 -> orderbook.editBid(bid.rate,bid.quantity)
                }
            }

            state.sells.forEach{ ask ->
                when(ask.type) {
                    0 -> orderbook.addAsk(ask.rate,ask.quantity)
                    1 -> orderbook.removeAsk(ask.rate)
                    2 -> orderbook.editAsk(ask.rate,ask.quantity)
                }
            }
        }

        val op = cycles
            //.forEach{cycle -> println(cycle.crossRate)}
            .filter{cycle -> cycle.crossRate() > BigDecimal.ONE}
            .maxBy{it.crossRate()}
            //.maxBy{it.value()}

        if (op != null){
            println(op)
            println("${op.crossRate()} xrate. ${op.startQuantity()} $base -> ${op.startQuantity() * op.crossRate()} $base ")
            op.getOrders().forEach {order -> 
                when (order){
                    is Bid -> println("BUYING  ${order.quantity} ${order.orderbook.quoteSymbol} @ ${order.rate} ON ${order.orderbook.quoteSymbol}/${order.orderbook.baseSymbol}")
                    is Ask -> println("SELLING ${order.quantity} ${order.orderbook.quoteSymbol} @ ${order.rate} ON ${order.orderbook.quoteSymbol}/${order.orderbook.baseSymbol}")
                }
            }
            println("")
        }
        else { 
            println("none :)")
        }
    }
}
    /*
    private fun prepareTrades(cycle: Cycle<String>, amount: BigDecimal): List<Order> {
        var rate = BigDecimal.ONE
        var startingTradeAmount = amount
        val orders: MutableList<Order> = mutableListOf()
        for (edge in cycle){
            for (pair in currencyPairs){
                if (isAskEdge(edge, pair)){
                    val book = bittrex.getOrderbook(pair)
                    val bestAsk = book.getBestAsk()
    
                    if(bestAsk != null){
                        rate = rate.multiply(bestAsk.rate,mathContext)
                        orders.add(Bid(pair,edge.end,BigDecimal.ZERO,bestAsk.rate))
                        if (startingTradeAmount * rate > bestAsk.quantity) 
                            startingTradeAmount = BigDecimal.ONE.divide(rate,9,BigDecimal.ROUND_HALF_UP).multiply(bestAsk.quantity, mathContext)
                    }
                    break
                }
                else if (isBidEdge(edge, pair)){
                    val book = bittrex.getOrderbook(pair)
                    val bestBid = book.getBestBid()

                    //sell x quote @ bid
                    if(bestBid != null){
                        rate = rate.multiply(BigDecimal.ONE.divide(bestBid.rate,9,BigDecimal.ROUND_HALF_UP), mathContext)
                        orders.add(Ask(pair,edge.begin,BigDecimal.ZERO,bestBid.rate))
                        if (startingTradeAmount * rate > bestBid.quantity)
                            startingTradeAmount = BigDecimal.ONE.divide(rate,9,BigDecimal.ROUND_HALF_UP).multiply(bestBid.quantity, mathContext)
                    }
                    break
                }
            }
        }
        var x = startingTradeAmount
        
        return orders.map{
            when(it){
                is Bid -> {
                    val bid = Bid(it.pair,it.name,x.multiply(BigDecimal.ONE.divide(it.rate,9,BigDecimal.ROUND_HALF_UP),mathContext), it.rate)
                    x = x.multiply(BigDecimal.ONE.divide(it.rate,9,BigDecimal.ROUND_HALF_UP) * BigDecimal(1.0 - tradeFees), mathContext)
                    bid
                }
                is Ask -> {
                    val ask = Ask(it.pair,it.name,x,it.rate)
                    x = x.multiply(it.rate * BigDecimal(1.0 - tradeFees), mathContext)
                    ask
                }
            }
        }
    }

    private fun isAskEdge(edge: DirectedEdge<String>, pair: BittrexCurrencyPair): Boolean  = (edge.begin == pair.base) && (edge.end == pair.quote)
    private fun isBidEdge(edge: DirectedEdge<String>, pair: BittrexCurrencyPair): Boolean  = (edge.begin == pair.quote) && (edge.end == pair.base)
}*/
