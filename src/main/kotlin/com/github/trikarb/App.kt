package com.github.trikarb

import kotlin.system.measureTimeMillis
import kotlin.system.measureNanoTime
import java.math.BigDecimal
import com.github.ccob.bittrex4j.BittrexExchange

fun main(args: Array<String>){
    val bittrexExchange = BittrexExchange()
    
    //init orderbook
    val markets = bittrexExchange.marketSummaries.result.map{marketSummary -> marketSummary.market.marketName}
    val orderbooks = markets.map{name ->
        val base = name.split("-")[0]
        val quote = name.split("-")[1]
        name to Orderbook(base,quote)
    }.toMap()

    val edges: List<DirectedEdge> = orderbooks.values.toList().asEdgeList()

    bittrexExchange.onUpdateExchangeState({updateExchangeState -> 
        val name = updateExchangeState.marketName
        val book = orderbooks[name]
        if (book != null){
            updateExchangeState.buys.forEach{bid -> 
                if (bid.type == 0)
                    book.addBid(bid.rate,bid.quantity)
                if (bid.type == 1)
                    book.removeBid(bid.rate)
                if (bid.type == 2)
                    book.editBid(bid.rate,bid.quantity)
            }
            updateExchangeState.sells.forEach{ask ->
                if (ask.type == 0)
                    book.addAsk(ask.rate,ask.quantity)
                if (ask.type == 1)
                    book.removeAsk(ask.rate)
                if (ask.type == 2)
                    book.editAsk(ask.rate,ask.quantity)
            }
            book.notifyObservers()
        }
        println("N: ${updateExchangeState.nounce} ${updateExchangeState.marketName}")
    })
    bittrexExchange.connectToWebSocket({
        
        orderbooks.forEach {(name,book) ->
            bittrexExchange.queryExchangeState(name,{})
            bittrexExchange.subscribeToExchangeDeltas(name,null)
        }
    })
}
