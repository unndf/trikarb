package com.github.trikarb

import java.math.BigDecimal

public class ArbitrageCycle(val edges: List<DirectedEdge>){
    
    public fun getCrossRate() = edges
        .map {edge -> edge.rate}
        .reduce {product, rate -> product * rate}
}

public fun List<DirectedEdge>.getArbitrageCycles(start: String, length: Int=3): List<ArbitrageCycle> {
    val cycles: MutableSet<ArbitrageCycle> = mutableSetOf()
    return cycles.toList()
}
