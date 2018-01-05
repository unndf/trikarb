package com.github.trikarb

import java.util.Observer
import java.util.Observable
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP

public class DirectedEdge(val orderbook: Orderbook, val reverseDirection: Boolean=false, val scale:Int=9): Observer {
    //getters and setters for rate and quantity are explicity defined
    //gets need to return a copy to reduce side-effects after calling get
    //forces the use of update* methods to change rate and quantity

    var rate: BigDecimal = BigDecimal.ZERO
        get() = rate.setScale(scale, ROUND_HALF_UP)
        private set
    
    var quantity: BigDecimal =  BigDecimal.ZERO
        get() = quantity.setScale(scale, ROUND_HALF_UP)
        private set
        
    init{
        updateRate()
        updateQuantity()
        orderbook.addObserver(this)
    }

    public fun getBaseSymbol(): String = orderbook.baseSymbol
    
    public fun getQuoteSymbol(): String = orderbook.quoteSymbol
    
    public fun updateRate(){
        if(!reverseDirection){
            val bestBidRate = orderbook.getBestBidRate()
            rate = if(bestBidRate != null) 
                    (bestBidRate).setScale(scale,ROUND_HALF_UP) 
                else 
                    BigDecimal.ZERO
        }
        else {
            val bestAskRate = orderbook.getBestAskRate()
            rate = if(bestAskRate != null) 
                    (BigDecimal.ONE / bestAskRate).setScale(scale, ROUND_HALF_UP)
                else 
                    BigDecimal.ZERO
        }
    }

    public fun updateQuantity(){
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

    public override fun update(orderbook: Observable, arg: Any) {
        updateRate()
        updateQuantity()
    }
}

public fun List<Orderbook>.asEdgeList(): List<DirectedEdge> {
    return this.flatMap{book -> listOf(DirectedEdge(book),DirectedEdge(book, reverseDirection=true))}
}
