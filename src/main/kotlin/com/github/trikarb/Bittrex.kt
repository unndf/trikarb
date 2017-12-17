package com.github.trikarb

import java.net.URL
import java.math.BigDecimal
import java.io.IOException
import com.fasterxml.jackson.module.kotlin.*
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonIgnoreProperties(ignoreUnknown=true)
data class CurrencyPair(
    @JsonProperty("BaseCurrency") val base: String,
    @JsonProperty("MarketCurrency") val quote: String,
    @JsonProperty("MinTradeSize") val MinTradeSize: BigDecimal  = BigDecimal(0)){
        override fun toString(): String = "${this.base}-${this.quote}"
    }
    
@JsonIgnoreProperties(ignoreUnknown=true)
data class Currency(
    @JsonProperty("Currency") val name: String,
    @JsonProperty("CurrencyLong") val verboseName: String,
    @JsonProperty("MinConfrimation") val minimumConfirmation: Int,
    @JsonProperty("TxFee") val txFee: BigDecimal
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class Tick(
    @JsonProperty("Bid") val bid: BigDecimal,
    @JsonProperty("Ask") val ask: BigDecimal,
    @JsonProperty("Last") val last: BigDecimal
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class Entry(
    @JsonProperty("Quantity") val quantity: BigDecimal,
    @JsonProperty("Rate") val rate: BigDecimal
    )

@JsonIgnoreProperties(ignoreUnknown=true)
data class Orderbook(
    @JsonProperty("buy") val bid: List<Entry?>,
    @JsonProperty("sell") val ask: List<Entry?>
    )

class BittrexPublic(version: String = "v1.1"){
    val version = version
    val mapper = jacksonObjectMapper()
    
    //Sorta Kludgy
    //the result param is nullable due to the cases where success==false and result==null
    data class GetMarketResponse(
        val success: Boolean,
        val message: String,
        val result: List<CurrencyPair>?
    )
    
    data class GetCurrenciesResponse(
        val success: Boolean,
        val message: String,
        val result: List<Currency>?
    )

    data class GetTickerResponse(
        val success: Boolean,
        val message: String,
        val result: Tick?
    )

    data class GetOrderbookResponse(
        val success: Boolean,
        val message: String,
        val result: Orderbook?
    )

    fun getMarkets(): List<CurrencyPair> {   
        val response = mapper.readValue<GetMarketResponse>(
            URL("https://bittrex.com/api/$version/public/getmarkets")
        )

        if (response.success && (response.result != null))
            return response.result
        else 
            throw IOException("$version/getmarkets REST query was unsuccessful")
    }

    fun getCurrencies(): List<Currency> {
        val response = mapper.readValue<GetCurrenciesResponse>(
            URL("https://bittrex.com/api/$version/public/getcurrencies")
        )

        if (response.success && (response.result != null))
            return response.result
        else 
            throw IOException("$version/getcurrencies REST query was unsuccessful")
    }

    fun getTicker(market: CurrencyPair): Tick {
        val response = mapper.readValue<GetTickerResponse>(
            URL("https://bittrex.com/api/$version/public/getticker?market=${market.base}-${market.quote}")
        )

        if (response.success && (response.result != null))
            return response.result
        else 
            throw IOException("$version/getticker REST query was unsuccessful")
    }

    fun getOrderbook(market: CurrencyPair): Orderbook {
        val response = mapper.readValue<GetOrderbookResponse>(
                URL("https://bittrex.com/api/$version/public/getorderbook?market=${market.base}-${market.quote}&type=both")
        )
        
        if (response.success && (response.result != null))
            return response.result
        else throw IOException("$version/getorderbook REST query was unsuccessful\n${response.message}")
    }
}
