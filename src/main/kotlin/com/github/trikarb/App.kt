package com.github.trikarb

import java.math.BigDecimal
import com.github.ccob.bittrex4j.BittrexExchange

fun main(args: Array<String>){
    val bittrexExchange = BittrexExchange()
    
    //create a list of all markets
    val markets = bittrexExchange.marketSummaries.result.map{marketSummary -> marketSummary.market.marketName}
    
    //create a trader for market (args[0])
    val trader = Trader(markets, args[0])
    
    bittrexExchange.onUpdateExchangeState({updateExchangeState -> 
        trader.updateOrderbook(updateExchangeState.marketName, updateExchangeState)
    })
    
    bittrexExchange.connectToWebSocket({
        val bittrexTemp = BittrexExchange()
        var count = 0
        trader.markets.forEach {name ->
            bittrexExchange.queryExchangeState(name, {updateExchangeState ->
                trader.updateOrderbook(name, updateExchangeState)
            })
            bittrexExchange.subscribeToExchangeDeltas(name,null)
        }
    })
}
