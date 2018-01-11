package com.github.trikarb

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import java.util.Observable

public class ArbitrageCycle(val edges: List<DirectedEdge>, val tradeFees: BigDecimal=BigDecimal(0.0025), val scale:Int=9){
   
    val orderbooks: HashSet<Orderbook> = edges.map { edge -> edge.orderbook }.toHashSet()

    var crossRate: BigDecimal = BigDecimal.ONE
        private set

    init {
        update()
    }

    public fun update() {
        updateCrossRate()
    }

    private fun updateCrossRate() {
        val order = getOrders().last()
        
        if (order != null){
            val start = startQuantity()
            val end = if (order is Bid) order.quantity else order.quantity * order.rate
            crossRate = if (end != BigDecimal.ZERO.setScale(scale,ROUND_HALF_UP)) end / start else BigDecimal.ZERO
        }
        else
            crossRate = BigDecimal.ZERO.setScale(scale,ROUND_HALF_UP)
    }

    public fun getOrders(): List<Order> {
        val orders: MutableList<Order> = mutableListOf()
        var quantity = startQuantity() 
        for (edge in edges){
            val bestBidRate = edge.orderbook.getBestBidRate() ?: BigDecimal.ONE
            val bestAskRate = edge.orderbook.getBestAskRate() ?: BigDecimal.ONE

            quantity = if (edge.reverseDirection) quantity else quantity * BigDecimal.ONE / bestAskRate
           
            quantity = quantity.setScale(scale, ROUND_HALF_UP)
            orders.add(if (edge.reverseDirection) Ask(bestBidRate,quantity,edge.orderbook) else Bid(bestAskRate,quantity,edge.orderbook))
           
            if (edge.reverseDirection)
                quantity *= bestBidRate

            quantity *= (BigDecimal.ONE - tradeFees)
        }
        return orders.toList()
    }

    public fun value(): BigDecimal = (startQuantity() * (crossRate - BigDecimal.ONE)).setScale(scale, ROUND_HALF_UP)

    public fun startQuantity(): BigDecimal {
        val start: BigDecimal? = 
            edges.mapIndexed { index, edge -> 
                edge.quantity * edges
                    .subList(0,index)
                    .map { it.rate }
                    .fold(BigDecimal.ONE) { xrate, r -> r}.setScale (scale, ROUND_HALF_UP)
            }
            .min()
        return BigDecimal.ONE.setScale(scale,ROUND_HALF_UP)
        //return if (start != null) start.setScale(scale,ROUND_HALF_UP) else BigDecimal.ZERO
    }

    public override fun toString(): String{
        return edges.joinToString(separator=" ")
    }
}

public fun List<DirectedEdge>.getArbitrageCycles(begin: String, length: Int=3): List<ArbitrageCycle> {
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
        .map{ArbitrageCycle(it)}
}

public fun List<DirectedEdge>.outEdges(vertex: String): List<DirectedEdge> = this.filter{edge -> edge.begin == vertex}

public fun List<DirectedEdge>.isElementary(): Boolean {
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
