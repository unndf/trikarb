package com.github.trikarb

import com.github.trikarb.util.inverse
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.util.Observable

public class ArbitrageCycle(
    val edges: List<DirectedEdge>, 
    val balance: BigDecimal = BigDecimal.ONE,
    val tradeFees: BigDecimal = BigDecimal("0.0025"),
    val scale: Int=8) {
    
    public fun crossRate(): BigDecimal {
        val order = getOrders().last()
        val start = startQuantity()
        val end = if (order is Bid) 
                order.quantity.setScale(scale,ROUND_HALF_UP)  
            else 
                (order.quantity * order.rate).setScale(scale,ROUND_HALF_UP)
         
        return if (end != BigDecimal.ZERO.setScale(scale,ROUND_HALF_UP))
                (end / start).setScale(scale,ROUND_HALF_UP) 
            else 
                BigDecimal.ZERO.setScale(scale,ROUND_HALF_UP)
    }

    //TODO: Refactor. This method has too much funtionality that should be handled by the orderbook
    public fun getOrders(): List<Order> {
        val orders: MutableList<Order> = mutableListOf()
        var quantity = startQuantity() 
        for (edge in edges){
            val bestBidRate = edge.orderbook.getBestBidRate() ?: BigDecimal.ONE.setScale(scale, ROUND_HALF_UP)
            val bestAskRate = edge.orderbook.getBestAskRate() ?: BigDecimal.ONE.setScale(scale, ROUND_HALF_UP)

            quantity = if (edge.reverse) quantity else quantity * bestAskRate.inverse()
            quantity *= (BigDecimal.ONE - edge.orderbook.tradeFee)
            
            quantity = quantity.setScale(scale, ROUND_HALF_UP)
            orders.add(if (edge.reverse) Ask(bestBidRate,quantity,edge.orderbook) else Bid(bestAskRate,quantity,edge.orderbook))
           
            if (edge.reverse)
                quantity *= bestBidRate

        }
        return orders.toList()
    }

    public fun value(): BigDecimal = (startQuantity() * (crossRate() - BigDecimal.ONE)).setScale(scale, ROUND_HALF_UP)

    public fun startQuantity(): BigDecimal {
        val start: BigDecimal? = 
            edges.mapIndexed { index, edge -> 
                edge.quantity() * edges
                    .subList(0,index)
                    .map { it.weight() }
                    .fold(BigDecimal.ONE) { xrate, r -> r }.setScale (scale, ROUND_HALF_UP)
            }
            .min()
        return if (start != null) 
                balance.setScale(scale,ROUND_HALF_UP).min(start.setScale(scale,ROUND_HALF_UP))
            else 
                BigDecimal.ZERO
    }

    public override fun toString(): String{
        return edges.joinToString(separator=" ")
    }
}

public fun List<DirectedEdge>.getArbitrageCycles(begin: String, length: Int=3, balance: BigDecimal=BigDecimal.ONE): List<ArbitrageCycle> {
    var cycles: MutableSet<List<DirectedEdge>> = mutableSetOf()
    
    this.outEdges(begin).forEach{cycles.add(listOf(it))}

    var depth = 1
    while (depth < length){
        val ncycles: MutableSet<List<DirectedEdge>> = mutableSetOf()
        for (cycle in cycles){
            this.outEdges(cycle.last().end).forEach{edge -> ncycles.add(cycle + edge)}
        }
        cycles = ncycles
        depth++
    }

    //keep only the proper cycles
    return cycles
        .filter{it.last().end == begin}
        .filter{it.isElementary()}
        .map{ArbitrageCycle(it, balance)}
}

private fun List<DirectedEdge>.outEdges(vertex: String): List<DirectedEdge> = this.filter{edge -> edge.begin == vertex}

private fun List<DirectedEdge>.isElementary(): Boolean {
    val source = this.first().begin
    val seenVertices: MutableSet<String> = hashSetOf()
    for (edge in this){
        if (seenVertices.contains(edge.end) && edge.end != source)
            return false
        
        if((edge != this.last() && edge != this.first()) && (edge.end == source || edge.begin == source))
            return false
        
        seenVertices.add(edge.begin)
        seenVertices.add(edge.end)
    }
    return true
}
