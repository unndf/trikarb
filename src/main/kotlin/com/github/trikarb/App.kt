package com.github.trikarb

fun main(args: Array<String>){
    //create a trader for market (args[0])
    if (args.size == 3) {
        Trader(args[0], args[1], args[2])
    }
    else {
        //display help message
        println("Usage karbitrageur <BASE CURRENCY> <PRIVATE KEY> <PRIVATE SECRET>")
    }
}
