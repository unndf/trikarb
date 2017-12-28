package com.github.trikarb

import java.math.BigDecimal

fun main(args: Array<String>){
    val trader = Trader(args[0],BigDecimal(10.0))
    trader.triangularArbitrage()
}
