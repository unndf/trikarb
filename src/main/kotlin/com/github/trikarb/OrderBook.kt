package com.github.trikarb

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.util.SortedMap
import java.util.TreeMap
import java.util.Observable

class Orderbook (val baseSymbol: String, val quoteSymbol: String, val scale: Int=9): Observable(){
    val bid: SortedMap<BigDecimal, BigDecimal> = TreeMap()
    val ask: SortedMap<BigDecimal, BigDecimal> = TreeMap()

    public fun getBestBidRate(): BigDecimal? {
        return if (!bid.isEmpty()) bid.firstKey() else null
    }

    public fun getBestBidQuantity(): BigDecimal?{
        return if (!bid.isEmpty()) bid[bid.firstKey()] else null
    }

    public fun getBestAskRate(): BigDecimal? {
        return if (!ask.isEmpty()) ask.lastKey() else null
    }

    public fun getBestAskQuantity(): BigDecimal? {
        return if (!ask.isEmpty()) ask[ask.lastKey()] else null
    }
    
    public fun editBid(rate: BigDecimal, quantity: BigDecimal) {
        bid[rate] = quantity.setScale(scale,ROUND_HALF_UP)
        this.setChanged()
    }

    public fun editBid(rate: Double, quantity: Double) {
        bid[BigDecimal(rate).setScale(scale,ROUND_HALF_UP)] = BigDecimal(quantity).setScale(scale,ROUND_HALF_UP)
        this.setChanged()
    }

    public fun editAsk(rate: BigDecimal, quantity: BigDecimal) {
        ask[rate] = quantity.setScale(scale,ROUND_HALF_UP)
        this.setChanged()
    }

    public fun editAsk(rate: Double, quantity: Double) {
        ask[BigDecimal(rate).setScale(scale,ROUND_HALF_UP)] = BigDecimal(quantity).setScale(scale,ROUND_HALF_UP)
        this.setChanged()
    }

    public fun addBid(rate: BigDecimal, quantity: BigDecimal) {
        editBid(rate, quantity)
        this.setChanged()
    }
    
    public fun addBid(rate: Double, quantity: Double) {
        editBid(BigDecimal(rate).setScale(scale,ROUND_HALF_UP), BigDecimal(quantity).setScale(scale,ROUND_HALF_UP))
        this.setChanged()
    }

    public fun addAsk(rate: BigDecimal, quantity: BigDecimal) {
        editAsk(rate, quantity)
        this.setChanged()
    }
    
    public fun addAsk(rate: Double, quantity: Double) {
        editAsk(BigDecimal(rate).setScale(scale,ROUND_HALF_UP), BigDecimal(quantity).setScale(scale,ROUND_HALF_UP))
        this.setChanged()
    }
    
    public fun removeBid(rate: BigDecimal) {
        bid.remove(rate)
        this.setChanged()
    }
    public fun removeBid(rate: Double) {
        bid.remove(BigDecimal(rate).setScale(scale,ROUND_HALF_UP))
        this.setChanged()
    }
    
    public fun removeAsk(rate: BigDecimal) {
        ask.remove(rate)
        this.setChanged()
    }

    public fun removeAsk(rate: Double){
        ask.remove(BigDecimal(rate).setScale(scale,ROUND_HALF_UP))
        this.setChanged()
    }

    public override fun toString(): String {
        val askString = ask.map{(rate, quantity) -> "$rate - $quantity"}.reversed().joinToString(separator="\n")
        val bidString = bid.map{(rate, quantity) -> "$rate - $quantity"}.joinToString(separator="\n")
        return "$baseSymbol-$quoteSymbol\nrate - quantity\nAsk:\n$askString\n\nBid:S$bidString"
    }
}
