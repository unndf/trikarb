package com.github.trikarb
import java.math.BigDecimal

data class DirectedEdge <T>(val begin: T, val end: T, val weight: BigDecimal){
    override fun toString(): String = "$begin-$end ($weight)"
}

typealias Cycle<T> = List<DirectedEdge<T>>
typealias Graph<T> = Set<DirectedEdge<T>>

fun <T> Graph<T>.getCycles(source: T, length: Int): Set<Cycle<T>> {
    var cycles: MutableSet<Cycle<T>> = mutableSetOf()
    val startEdges = this.filter{
        it.begin == source
    }

    for (edge in startEdges)
        cycles.add(listOf(edge))
        
    for(i in 1..length-1){
        val ncycles: MutableSet<Cycle<T>> = mutableSetOf()
        for (cycle in cycles){
            for (edge in this){
                if (cycle.last().end == edge.begin) ncycles.add(cycle + edge)
            }
        }
        cycles = ncycles.filter{it.isElementary()}.toMutableSet()
    }
    return cycles.filter{it.last().end == source}.toSet()
}

fun <T> Cycle<T>.isElementary(): Boolean {
    val source = this.first().begin
    val seen: MutableSet<T> = hashSetOf()
    for (edge in this){
        if (seen.contains(edge.end) && edge.end != source)
            return false
        
        if((edge != this.last() && edge != this.first()) && (edge.end == source || edge.begin == source))
            return false
        
        seen.add(edge.begin)
        seen.add(edge.end)
    }
    return true
}

fun <T> Cycle<T>.getCrossrate() = this.fold(BigDecimal(1.0)) {rate, edge -> rate * edge.weight}
