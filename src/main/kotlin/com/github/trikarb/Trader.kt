package com.github.trikarb

import java.math.BigDecimal
import kotlin.system.measureTimeMillis

data class Order(val name: String, val amount: BigDecimal, val rate: BigDecimal)
sealed class Order
{
    data class Bid(val name: String, val amount: BigDecimal, val rate: BigDecimal)
    data class Ask(val name: String, val amount: BigDecimal, val rate: BigDecimal)
}

class Trader(base: String, maxTradeSize: BigDecimal){
    var trading = false
    val baseCurrency = base
    val maxTradeSize = maxTradeSize
    val tradeFees = 0.25 * 0.01 //hardcoded for Bittrex
    val bittrex = Bittrex()
    val currencyPairs = bittrex.getMarkets()

    fun triangularArbitrage() { 
        var cycles: Set<Cycle<String>> = setOf()
        var graph = bittrex.getMarketGraph()
        
        //start trading
        trading = true
        
        while(trading){
            graph = bittrex.getMarketGraph()
            cycles = graph.getCycles(baseCurrency,3)
            
            val arbOps = cycles.filter{
                it.getCrossrate() > BigDecimal(1.0 + 3.0 * tradeFees)
            }
            val bestOp = arbOps.maxBy{it.getCrossrate()}
            if (bestOp != null){
                println("\n$bestOp @${bestOp.getCrossrate()}")
                getOrderBooks(bestOp, maxTradeSize)
            }
        }
    }

    private fun getOrderBooks(cycle: Cycle<String>, amount: BigDecimal){
        var rate = BigDecimal.ONE
        var startingTradeAmount = amount
        //val orders: MutableList<Pair<String,BigDecimal>> = mutableListOf()
        val orders: MutableList<Order> = mutableListOf()

        for (edge in cycle){
            for (pair in currencyPairs){
                if (isAskEdge(edge, pair)){
                    val book = bittrex.getOrderbook(pair)
                    val bestAsk = book.getBestAsk()
                    
                    //buy x quote @ ask
                    //need to sleep
                    //pls remember
                    //if base_amount * rateproduct????? then base_amount = base_amount * rateproduct^-1
                    //trust this makes sense
                    //also bids/ask can be zero, fix next
    
                    if(bestAsk != null){
                        rate *= bestAsk.rate
                        orders.add("BUY ${edge.end} " to bestAsk.rate)
                        if (startingTradeAmount * rate > bestAsk.quantity) 
                            startingTradeAmount = BigDecimal.ONE.divide(rate,9,BigDecimal.ROUND_HALF_UP) * bestAsk.quantity
                    }
                    break
                }
                else if (isBidEdge(edge, pair)){
                    val book = bittrex.getOrderbook(pair)
                    val bestBid = book.getBestBid()

                    //sell x quote @ bid
                    if(bestBid != null){
                        rate *= BigDecimal.ONE.divide(bestBid.rate,9,BigDecimal.ROUND_HALF_UP)
                        orders.add("SELL ${edge.begin} " to BigDecimal.ONE.divide(bestBid.rate,9,BigDecimal.ROUND_HALF_UP))
                        if (startingTradeAmount * rate > bestBid.quantity)
                            startingTradeAmount = BigDecimal.ONE.divide(rate,9,BigDecimal.ROUND_HALF_UP) * bestBid.quantity
                    }
                    break
                }
            }
        }
        for (edge in cycle){
            for (pair in currencyPairs){
                if (isAskEdge(edge,Pair){
                    
                    
                }
                else if (isBidEdge(edge,Pair)){
                }
            }
        }
        println("${orders[i].first} ${startingTradeAmount}")
        println("we start with $startingTradeAmount ETH")
        for (i in 1..orders.size-1)
            println("${order[i].first} ${order.second * startingTradeAmount}")
    }

    private fun isAskEdge(edge: DirectedEdge<String>, pair: BittrexCurrencyPair): Boolean  = (edge.begin == pair.base) && (edge.end == pair.quote)
    private fun isBidEdge(edge: DirectedEdge<String>, pair: BittrexCurrencyPair): Boolean  = (edge.begin == pair.quote) && (edge.end == pair.base)
}
