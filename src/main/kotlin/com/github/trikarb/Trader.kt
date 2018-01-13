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
 
    public fun updateOrderbook(name: String, state: UpdateExchangeState) {
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
            .filter{cycle -> cycle.crossRate() > BigDecimal.ONE}
            .maxBy{it.crossRate()}
        
        if (op != null){
            println(op)
            println("${op.crossRate()} xrate. ${op.startQuantity()} $base -> ${(op.startQuantity() * op.crossRate())} $base ")
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
