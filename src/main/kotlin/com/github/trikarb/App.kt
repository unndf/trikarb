package com.github.trikarb

import java.math.BigDecimal
import kotlin.math.ln
import kotlin.math.E
import kotlin.math.pow
import kotlin.system.measureTimeMillis

fun main(args: Array<String>){
    val bittrex = Bittrex()

    var exit = false
    while(!exit){
        var cycles: List<Cycle<String>> = listOf()
        val summarys = bittrex.getMarketSummaries()
        val edgesBids = summarys.map{
            val name = it.name.split("-")
            val start = name[0]
            val end = name[1]
            val weight = it.bid.toDouble()
            DirectedEdge<String>(start, end, weight)
        }

        val edgesAsks = summarys.map{
            val name = it.name.split("-")
            val start = name[1]
            val end = name[0]
            val weight = 1.0 / it.ask.toDouble()
            DirectedEdge<String>(start, end, weight)
        }
        val edges = edgesBids + edgesAsks
        val elapsed = measureTimeMillis{
            cycles = edges.getCycles("BTC",3)
        }
        
        val ops = cycles.filter{
            var crossrate = 1.0
            for (edge in it)
                crossrate *= edge.weight

            crossrate > 1.005
        }
        for (i in ops){
            var crossrate = 1.0
            for (edge in i)
                crossrate *= edge.weight
            println(i)
            println("x-rate = $crossrate")
        }
        println("took $elapsed ms to get all length 4 ETH cycles. WEW FUCKING LAD")
    }
//        val begins = summarys.map{
//            val name = it.name.split("-")
//            name[0]
//        }.toSet()
//
//        val ends = summarys.map{
//            val name = it.name.split("-")
//            name[1]
//        }.toSet()
//        
//        val vertices = begins.union(ends)
//        
//        val bfsresult = bellmanFord<String>("ETC", vertices, edges)
//        cycles = bfsresult.cycles
//        
//
//        val rates: MutableList<Double> = mutableListOf()
//        for (cycle in cycles){
//            for ((i,vert) in cycle.withIndex()){
//                val edge = findEdge(vert, cycle[(i+1) % cycle.size], edges)
//                if (edge != null){
//                    val weight = edge.weight
//                    val rate = E.pow(-weight)
//                    print("$vert/${cycle[(i+1) % cycle.size]} ")
//                    rates.add(rate)
//                } 
//                else {
//                    println("NO EGE!!!!!!!!!!")
//                    rates.clear()
//                }
//            }
//            println("")
//            var xrate = 1.0
//            for (rate in rates)
//                xrate *= rate
//
//            println("crossrate: $xrate")
//            rates.clear()
//
//        }
      //println("cycles $cycles")
      //        println("Took $elapsed ms. ${cycles.size} oppurtunites found")
//    }
}
