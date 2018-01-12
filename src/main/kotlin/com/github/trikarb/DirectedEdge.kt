package com.github.trikarb

import com.github.trikarb.util.inverse
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP

/**
An Edge on a Directed Graph

This edge is backed by an Order book which provides the values for the weight of the edge (rate of the best bid/ask in the book)

@param orderbook the backing Order Book
@param reverse boolean flag indicating whether this edge is a backedge or not
*/
public class DirectedEdge(val orderbook: Orderbook, val reverse: Boolean=false) {
   
    //Symbols for the start/end of an edge eg. BTC->ETH. BTC = start and ETH = end
    val begin: String = if (!reverse) orderbook.baseSymbol else orderbook.quoteSymbol
    val end: String = if (!reverse) orderbook.quoteSymbol else orderbook.baseSymbol
    
    val scale = orderbook.scale
    
    public fun weight(): BigDecimal {
        if(!reverse) {
            val bestAskRate = orderbook.getBestAskRate()
            return if (bestAskRate != null) 
                    bestAskRate.inverse()
                else
                    BigDecimal.ZERO
        } 
        else {
            val bestBidRate = orderbook.getBestBidRate()
            return if (bestBidRate != null) 
                    bestBidRate.setScale(scale, ROUND_HALF_UP)
                else
                    BigDecimal.ZERO
        }
    }

    public fun quantity(): BigDecimal {
        if(!reverse) {
            val bestBidQuantity = orderbook.getBestBidQuantity()
            return if (bestBidQuantity != null) 
                    bestBidQuantity
                else 
                    BigDecimal.ZERO
        }
        else {
            val bestAskQuantity = orderbook.getBestAskQuantity()
            return if(bestAskQuantity != null) 
                    bestAskQuantity
                else 
                    BigDecimal.ZERO
        }
    }

    public override fun toString(): String {
        return "$begin -> $end @ ${weight()}"
    }
}
