package com.github.trikarb

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.util.SortedMap
import java.util.TreeMap

class Orderbook (val baseSymbol: String, val quoteSymbol: String, val scale: Int=9){
    val bid: SortedMap<BigDecimal, BigDecimal> = TreeMap()
    val ask: SortedMap<BigDecimal, BigDecimal> = TreeMap()

    public fun getBestBidRate(): BigDecimal? {
        return if (bid.size > 0) bid.lastKey() else null
    }

    public fun getBestBidQuantity(): BigDecimal?{
        return if (bid.size > 0) bid[bid.lastKey()] else null
    }

    public fun getBestAskRate(): BigDecimal? {
        return if (ask.size > 0) ask.firstKey() else null
    }

    public fun getBestAskQuantity(): BigDecimal? {
        return if (ask.size > 0) ask[ask.firstKey()] else null
    }
    
    public fun editBid(rate: BigDecimal, quantity: BigDecimal) {
        bid[rate] = quantity.setScale(scale,ROUND_HALF_UP)
    }

    public fun editBid(rate: Double, quantity: Double) {
        bid[BigDecimal(rate).setScale(scale,ROUND_HALF_UP)] = BigDecimal(quantity).setScale(scale,ROUND_HALF_UP)
    }

    public fun editAsk(rate: BigDecimal, quantity: BigDecimal) {
        ask[rate] = quantity.setScale(scale,ROUND_HALF_UP)
    }

    public fun editAsk(rate: Double, quantity: Double) {
        ask[BigDecimal(rate).setScale(scale,ROUND_HALF_UP)] = BigDecimal(quantity).setScale(scale,ROUND_HALF_UP)
    }

    public fun addBid(rate: BigDecimal, quantity: BigDecimal) {
        editBid(rate, quantity)
    }
    
    public fun addBid(rate: Double, quantity: Double) {
        editBid(BigDecimal(rate).setScale(scale,ROUND_HALF_UP), BigDecimal(quantity).setScale(scale,ROUND_HALF_UP))
    }

    public fun addAsk(rate: BigDecimal, quantity: BigDecimal) {
        editAsk(rate, quantity)
    }
    
    public fun addAsk(rate: Double, quantity: Double) {
        editAsk(BigDecimal(rate).setScale(scale,ROUND_HALF_UP), BigDecimal(quantity).setScale(scale,ROUND_HALF_UP))
    }
    
    public fun removeBid(rate: BigDecimal) {
        bid.remove(rate)
    }

    public fun removeBid(rate: Double) {
        bid.remove(BigDecimal(rate).setScale(scale,ROUND_HALF_UP))
    }
    
    public fun removeAsk(rate: BigDecimal) {
        ask.remove(rate)
    }

    public fun removeAsk(rate: Double){
        ask.remove(BigDecimal(rate).setScale(scale,ROUND_HALF_UP))
    }

    public override fun toString(): String {
        val askString = ask.map{(rate, quantity) -> "$rate - $quantity"}.reversed().joinToString(separator="\n")
        val bidString = bid.map{(rate, quantity) -> "$rate - $quantity"}.joinToString(separator="\n")
        return "$baseSymbol-$quoteSymbol\nrate - quantity\nAsk:\n$askString\n\nBid:S$bidString"
    }
}

public fun List<Orderbook>.asEdgeList(): List<DirectedEdge> {
    return this.flatMap{book -> listOf(DirectedEdge(book),DirectedEdge(book, reverseDirection=true))}
}
