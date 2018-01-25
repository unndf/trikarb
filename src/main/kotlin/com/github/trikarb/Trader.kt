package com.github.trikarb

import java.math.BigDecimal
import java.math.MathContext
import kotlin.system.measureTimeMillis
import com.github.ccob.bittrex4j.BittrexExchange
import com.github.ccob.bittrex4j.dao.UpdateExchangeState
import com.github.ccob.bittrex4j.dao.MarketOrdersResult

class Trader(val base: String, val privateKey: String, val secret: String, maxCycleLength: Int=4){
    val bittrexExchange = BittrexExchange(privateKey, secret)
    
    //create a list of all markets
    val markets = bittrexExchange.marketSummaries.result.map{marketSummary -> marketSummary.market}
    val balance: BigDecimal = BigDecimal(bittrexExchange.getBalance(base).result.available)
    var executingTrades = false


    val orderbooks: Map<String, Orderbook> = markets.map{market ->
        val base = market.marketName.split("-")[0]
        val quote = market.marketName.split("-")[1]
        market.marketName to Orderbook(base,quote)
    }.toMap()
    
    val edges: List<DirectedEdge> = orderbooks.values.toList().asEdgeList()
    val cycles: List<ArbitrageCycle> = (3..maxCycleLength).flatMap{length -> edges.getArbitrageCycles(base,length=length,balance=balance)}

    init {
        println("BALANCE $balance")

        //create listeners
        bittrexExchange.onUpdateExchangeState({updateExchangeState -> 
            updateOrderbook(updateExchangeState.marketName, updateExchangeState)
        })
        
        bittrexExchange.connectToWebSocket({
            markets.forEach {market ->
                bittrexExchange.queryExchangeState(market.marketName, {updateExchangeState ->
                    updateOrderbook(market.marketName, updateExchangeState)
                })
                bittrexExchange.subscribeToExchangeDeltas(market.marketName,null)
            }
        })

    }    
    
    //TODO: refactor into orderbook
    public fun updateOrderbook(name: String, state: UpdateExchangeState) {
        val orderbook = orderbooks[name]
        val timeUpdating = measureTimeMillis{
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
        }
        val timeCalcCycles = measureTimeMillis {
            recalculateCycles()
        }
        println("time spent updating books: $timeUpdating, ${state.buys.size + state.sells.size} entries.")
        println("time spent clac cycles $timeCalcCycles")
    }

    private fun recalculateCycles() {
        val op = cycles
            .filter { cycle -> cycle.crossRate() > BigDecimal.ONE }
            .filter { 
                it.getOrders().all {
                    order -> 
                        val market = markets.find { market -> market.marketName == order.market}
                        if (market != null)
                            order.quantity.toDouble() > market.minTradeSize
                        else 
                            false
                } 
            }
            .maxBy { it.value() }
        
        if (op != null){
            if (!executingTrades) {
                println(op)
                println("${op.crossRate()} xrate. ${op.startQuantity()} $base -> ${(op.startQuantity() * op.crossRate())} $base ")
                executingTrades = true
                op.getOrders().forEach {order -> 
                    when (order){
                        is Bid -> {
                            println("BUYING  ${order.quantity} ${order.orderbook.quoteSymbol} @ ${order.rate} ON ${order.orderbook.quoteSymbol}/${order.orderbook.baseSymbol}")
                        /*
                            val resp = bittrexExchange.buyLimit(order.market, order.quantity.toDouble(), order.rate.toDouble())
                            if (resp.isSuccess()){
                                println("AYE!!!!")
                                val uuid = resp.result.uuid
                                var executed = false
                                while (!executed){
                                    val resp = bittrexExchange.getOrder(uuid)
                                    if (resp.isSuccess()){
                                        executed = !resp.result.isOpen()
                                        println("LIMIT EXECUTED is $executed")
                                    }
                                }
                            }
                            else{
                                println(resp.message)
                            }*/
                        }
                        is Ask -> {
                            println("SELLING ${order.quantity} ${order.orderbook.quoteSymbol} @ ${order.rate} ON ${order.orderbook.quoteSymbol}/${order.orderbook.baseSymbol}")
                            /*
                            val resp = bittrexExchange.sellLimit(order.market, order.quantity.toDouble(), order.rate.toDouble())
                            if (resp.isSuccess()){
                                println("AYE!!!!")
                                val uuid = resp.result.uuid
                                var executed = false
                                while (!executed){
                                    val resp = bittrexExchange.getOrder(uuid)
                                    if (resp.isSuccess()){
                                        executed = !resp.result.isOpen()
                                        println("LIMIT EXECUTED is $executed")
                                    }
                                }
                            }
                            else{
                                println(resp.message)
                            }*/
                        }
                    }
                    println("")
                }
                executingTrades = false
            }
        }
    }
}
