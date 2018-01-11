package com.github.trikarb

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP

public class DirectedEdge(val orderbook: Orderbook, val reverseDirection: Boolean=false, val scale:Int=9) {
    //getters and setters for rate and quantity are explicity defined
    //gets need to return a copy to reduce side-effects after calling get
    //forces the use of update* methods to change rate and quantity

    var rate: BigDecimal = BigDecimal.ZERO
        private set
    
    var quantity: BigDecimal =  BigDecimal.ZERO
        private set
    
    val begin: String = if (!reverseDirection) orderbook.baseSymbol else orderbook.quoteSymbol
    val end: String = if (!reverseDirection) orderbook.quoteSymbol else orderbook.baseSymbol

    init{
        updateRate()
        updateQuantity()
    }

    public fun getBaseSymbol(): String = orderbook.baseSymbol
    
    public fun getQuoteSymbol(): String = orderbook.quoteSymbol
    
    public fun update() {
        updateRate()
        updateQuantity()
    }

    private fun updateRate(){
        if(!reverseDirection){
            val bestBidRate = orderbook.getBestBidRate()
            rate = if(bestBidRate != null) 
                    (BigDecimal.ONE.setScale(scale,ROUND_HALF_UP) / bestBidRate).setScale(scale,ROUND_HALF_UP) 
                else
                    BigDecimal.ZERO
        }
        else {
            val bestAskRate = orderbook.getBestAskRate()
            rate = if(bestAskRate != null) 
                    bestAskRate.setScale(scale, ROUND_HALF_UP)
                else
                    BigDecimal.ZERO
        }
    }

    private fun updateQuantity(){
        if(!reverseDirection){
            val bestBidQuantity = orderbook.getBestBidQuantity()
            quantity = if(bestBidQuantity != null) 
                    (bestBidQuantity).setScale(scale,ROUND_HALF_UP) 
                else 
                    BigDecimal.ZERO
        }
        else {
            val bestAskQuantity = orderbook.getBestAskQuantity()
            quantity = if(bestAskQuantity != null) 
                    (bestAskQuantity).setScale(scale, ROUND_HALF_UP)
                else 
                    BigDecimal.ZERO
        }
    }

    public override fun toString(): String {
        return "$begin -> $end @ $rate"
    }
}
