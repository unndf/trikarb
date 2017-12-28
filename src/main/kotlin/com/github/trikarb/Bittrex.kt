package com.github.trikarb

import java.net.URL
import java.math.BigDecimal
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonIgnoreProperties(ignoreUnknown=true)
data class BittrexCurrencyPair(
    @JsonProperty("BaseCurrency") val base: String,
    @JsonProperty("MarketCurrency") val quote: String,
    @JsonProperty("MinTradeSize") val MinTradeSize: BigDecimal  = BigDecimal(0)){
        override fun toString(): String = "${this.base}-${this.quote}"
    }
    
@JsonIgnoreProperties(ignoreUnknown=true)
data class BittrexCurrency(
    @JsonProperty("Currency") val name: String,
    @JsonProperty("CurrencyLong") val verboseName: String,
    @JsonProperty("MinConfrimation") val minimumConfirmation: Int,
    @JsonProperty("TxFee") val txFee: BigDecimal
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class BittrexTick(
    @JsonProperty("Bid") val bid: BigDecimal,
    @JsonProperty("Ask") val ask: BigDecimal,
    @JsonProperty("Last") val last: BigDecimal
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class BittrexMarketSummary(
    @JsonProperty("MarketName") val name: String,
    @JsonProperty("Bid") val bid: BigDecimal,
    @JsonProperty("Ask") val ask: BigDecimal,
    @JsonProperty("Last") val last: BigDecimal,
    @JsonProperty("High") val high: BigDecimal,
    @JsonProperty("Low") val low: BigDecimal,
    @JsonProperty("Volume") val volume: BigDecimal,
    @JsonProperty("OpenBuyOrders") val openBids: Int,
    @JsonProperty("OpenSellOrders") val openAsks: Int
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class BittrexEntry(
    @JsonProperty("Quantity") val quantity: BigDecimal,
    @JsonProperty("Rate") val rate: BigDecimal
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class BittrexOrderbook(
    @JsonProperty("buy") val bid: List<BittrexEntry>,
    @JsonProperty("sell") val ask: List<BittrexEntry>
    ){
        override fun toString(): String = "--- Ask ---\n${this.ask.reversed().joinToString(separator="\n")}\n\n--- Bid ---\n${this.bid.joinToString(separator="\n")}"
        
        fun getBestBid(): BittrexEntry? = this.bid.maxBy{it.rate}
        fun getBestAsk(): BittrexEntry? = this.ask.minBy{it.rate}
    }

private data class Response <T>(
        val success: Boolean,
        val message: String,
        val result: T
    )

class Bittrex(version: String = "v1.1"){
    val version = version
    val mapper = jacksonObjectMapper()
    val urlBase = "https://bittrex.com/api/$version"
    
    fun getMarketGraph(): Graph<String> {
        val summarys = this.getMarketSummaries()
        
        //create edges out of all the bids
        //filter out bids < 0
        val edgesBids: Graph<String> = summarys.filter{it.bid > BigDecimal.ZERO}.map{
            val name = it.name.split("-")
            val start = name[0]
            val end = name[1]
            val weight = it.bid
            DirectedEdge<String>(start, end, weight)
        }.toSet()

        //create edges out of all the asks
        //filter out asks < 0
        val edgesAsks: Graph<String> = summarys.filter{it.ask > BigDecimal.ZERO}.map{
            val name = it.name.split("-")
            val start = name[1]
            val end = name[0]
            val weight = BigDecimal.ONE.divide(it.ask,9,BigDecimal.ROUND_HALF_UP)
            DirectedEdge<String>(start, end, weight)
        }.toSet()

        return edgesBids.union(edgesAsks)
    }

    fun getMarkets(): List<BittrexCurrencyPair> {
        val response = mapper.readValue<Response<List<BittrexCurrencyPair>>>(URL("$urlBase/public/getmarkets"))
        return verify<List<BittrexCurrencyPair>>(response)
    }
    fun getCurrencies(): List<BittrexCurrency> {
        val response = mapper.readValue<Response<List<BittrexCurrency>>>(URL("$urlBase/public/getcurrencies"))
        return verify<List<BittrexCurrency>>(response)
    }

    fun getMarketSummaries(): List<BittrexMarketSummary> {
        val response = mapper.readValue<Response<List<BittrexMarketSummary>>>(URL("$urlBase/public/getmarketsummaries"))
        return verify<List<BittrexMarketSummary>>(response)
    }

    fun getTicker(market: BittrexCurrencyPair): BittrexTick {
        val response = mapper.readValue<Response<BittrexTick>>(URL("$urlBase/public/getticker?market=$market"))
        return verify<BittrexTick>(response)
    }

    fun getOrderbook(market: BittrexCurrencyPair): BittrexOrderbook {
        val response = mapper.readValue<Response<BittrexOrderbook>>(URL("$urlBase/public/getorderbook?market=$market&type=both"))
        return verify<BittrexOrderbook>(response)
    }

    private fun <T> verify(response: Response<T>): T {
        if (response.success && (response.result != null)){
           //println(response.result.javaClass.kotlin.qualifiedName)
           return response.result
        }
        else throw IOException("REST Query was not successful. Got Message: ${response.message}")
    }

    private fun <T> request(requestUrl: String): T {
        val response = mapper.readValue<Response<T?>> (URL("$urlBase$requestUrl"))

        if (response.success && (response.result != null)){
            //println(response.result.javaClass.kotlin.qualifiedName)
            return response.result
        }
        else throw IOException("$requestUrl REST Query was not successful. Got Message: ${response.message}")
    }
}
