package com.github.trikarb

import java.math.BigDecimal

data class DirectedEdge <T>(val begin: T, val end: T, val weight: Double){
    override fun toString(): String = "$begin-$end ($weight)"
}

data class BFTuple <T>(val distance: Map<T,Double>, val predecessors: Map<T,T?>, val cycles: List<List<T>>)

typealias Cycle<T> = List<DirectedEdge<T>>
typealias Graph<T> = List<DirectedEdge<T>>

fun <T> Graph<T>.getCycles(source: T, length: Int): List<Cycle<T>> {
    var cycles: MutableList<Cycle<T>> = mutableListOf()
    val startEdges = this.filter{
        it.begin == source
    }

    for (edge in startEdges)
        cycles.add(listOf(edge))
        
    for(i in 1..length-1){
        val ncycles: MutableList<Cycle<T>> = mutableListOf()
        for (cycle in cycles){
            for (edge in this){
                if (cycle.last().end == edge.begin) ncycles.add(cycle + edge)
            }
        }
        cycles = ncycles.filter{it.isElementary()}.toMutableList()
    }
    return cycles.filter{it.last().end == source}
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
/*
fun <T> bellmanFord(source: T, vertices: Set<T>, edges: List<DirectedEdge<T>>): BFTuple<T> {

    //Set all out distances to  +Inf (except for source which is Set to 0)
    val distances: MutableMap<T,Double> = vertices.associate{
        if (it == source) it to 0.0 else it to Double.POSITIVE_INFINITY
    }.toMutableMap()

    //Set all our predecessors to null
    val predecessors: MutableMap<T,T?> = vertices.associate{
        it to null
    }.toMutableMap()
    
    for (itr in 1..vertices.size -1){
        for (edge in edges){
            val begin = edge.begin
            val end = edge.end
            val weight = edge.weight
            
            //relax all edges
            val distEnd = distances[end]!!
            val distBegin = distances[begin]!!

            if(distEnd > (distBegin + weight)){
                distances[end] = distBegin + weight
                predecessors[end] = begin
            }
        }
    }

    val cycles = mutableSetOf<List<T>>()
    //find negative cycles
    for (edge in edges) {
        val begin = edge.begin
        val end = edge.end
        val weight = edge.weight
        
        //val distEnd = distances.getOrDefault(end,Double.POSITIVE_INFINITY)
        //val distBegin = distances.getOrDefault(begin,Double.POSITIVE_INFINITY)
        
        val distEnd = distances[end]!!
        val distBegin = distances[begin]!!
        
        if(distEnd > (distBegin + weight)){
            val cycle = mutableListOf<T>()
            var next = predecessors[begin]
            
            while((next !in cycle) && next != null) {
                cycle.add(next)
                next = predecessors[next]
            }
            cycles.add(cycle.reversed())
        }
    }
    return BFTuple(distances, predecessors, cycles.toList())
}
*/
fun <T> findEdge(begin: T, end: T, edges: List<DirectedEdge<T>>): DirectedEdge<T>? {
    for (edge in edges)
        if(edge.begin == begin && edge.end == end)
            return edge

    return null
}
