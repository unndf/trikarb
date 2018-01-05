package com.github.trikarb

import java.math.BigDecimal
import java.math.MathContext
import kotlin.system.measureTimeMillis

sealed class Order

data class Bid(val pair: BittrexCurrencyPair, val name: String, val amount: BigDecimal, val rate: BigDecimal): Order()
data class Ask(val pair: BittrexCurrencyPair, val name: String, val amount: BigDecimal, val rate: BigDecimal): Order()
/*
class Trader(base: String, maxTradeSize: BigDecimal){
    var trading = false
    val baseCurrency = base
    val maxTradeSize = maxTradeSize
    val tradeFees = 0.25 * 0.01 //hardcoded for Bittrex
    val bittrex = Bittrex()
    val currencyPairs = bittrex.getMarkets()
    val mathContext = MathContext(9)

    fun triangularArbitrage() { 
        var cycles: Set<Cycle<String>> = setOf()
        var graph = bittrex.getMarketGraph()
        
        graph = bittrex.getMarketGraph()
        cycles = graph.getCycles(baseCurrency,3)
            .union(graph.getCycles(baseCurrency,4))
        
        //start trading
        trading = true
        while(trading){
            var timeCycles: Long = 0
            graph = bittrex.getMarketGraph()
            cycles = cycles.map{it.updateWeights(graph)}.toSet()
            val arbitrageOps = cycles.filter{
                    it.getCrossrate() > BigDecimal(1.0 + it.size * tradeFees)
            }

            val bestOp = arbitrageOps.maxBy{it.getCrossrate()}
            
            if (bestOp != null){
                val elapse = measureTimeMillis{
                    val trades = prepareTrades(bestOp, maxTradeSize)
                }
                println("$elapse (ms) preparing trades")
            }
        } 
    }

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
