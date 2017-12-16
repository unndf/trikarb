package com.github.trikarb

import java.net.URL
import java.math.BigDecimal
import java.io.InputStream
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.annotation.*

@JsonIgnoreProperties(ignoreUnknown=true)
data class CurrencyPair(
    
    @JsonProperty("BaseCurrency")
    val base: String,

    @JsonProperty("MarketCurrency")
    val quote: String,
    
    @JsonProperty("MinTradeSize")
    val MinTradeSize: BigDecimal)

@JsonIgnoreProperties(ignoreUnknown=true)
data class Currency(
    
    @JsonProperty("Currency")
    val name: String,

    @JsonProperty("CurrencyLong")
    val verboseName: String,
    
    @JsonProperty("MinConfrimation")
    val minimumConfirmation: Int,
    
    @JsonProperty("TxFee")
    val txFee: BigDecimal)

@JsonIgnoreProperties(ignoreUnknown=true)
data class Tick(
    
    @JsonProperty("Bid")
    val bid: BigDecimal,
    
    @JsonProperty("Ask")
    val ask: BigDecimal,
    
    @JsonProperty("Last")
    val last: BigDecimal)

data class Entry (val rate: BigDecimal, val amount: BigDecimal)

data class OrderBook (val bid: List<Entry>, val ask: List<Entry>)

fun main(args: Array<String>)
{
    val bittrex = BittrexPublic()
    for (market in bittrex.getMarkets())
        println(market)

    for (currency in bittrex.getCurrencies()){
        println(currency)
}

class BittrexMarket(){
}
class BittrexPublic(version: String = "v1.1"){
    val version = version
    val mapper = jacksonObjectMapper()
    
    //Sorta Kludgy
    //TODO: Create Some sort of generalized Response dataWrapper and generalized response
    //sealed classes?

    data class GetMarketResponse(
        val success: Boolean,
        val message: String,
        val result: List<CurrencyPair>)
    
    data class GetCurrenciesResponse(
        val success: Boolean,
        val message: String,
        val result: List<Currency>)

    data class GetTickerResponse(
        val success: Boolean,
        val message: String,
        val result: Tick)

    data class getTickerResponse(
        val success: Boolean,
        val message: String,
        val result: Tick)

    fun getMarkets(): List<CurrencyPair> {   
        val stream = request(URL("https://bittrex.com/api/$version/public/getmarkets"))
        val response = mapper.readValue<GetMarketResponse>(stream)

        if (!response.success) throw IOException("$version/getmarkets REST query was unsuccessful")

        return response.result
    }
    fun getCurrencies(): List<Currency> {
        val stream = request(URL("https://bittrex.com/api/$version/public/getcurrencies"))
        val response = mapper.readValue<GetCurrenciesResponse>(stream)
        
        if (!response.success) throw IOException("$version/getcurrencies REST query was unsuccessful")

        return response.result
    }

    fun getTicker(market: CurrencyPair): Tick {
        val stream = request(URL("https://bittrex.com/api/$version/public/getticker?market${market.base}-${market.quote}"))
        val response = mapper.readValue<GetTickerResponse>(stream)
        
        if (!response.success) throw IOException("$version/getticker REST query was unsuccessful")

        return response.result
       
    }
    //fun getOrderBook(market: AssetPair): OrderBook {
    //}
    
    private fun request(url: URL): InputStream  = url.openStream()
}
