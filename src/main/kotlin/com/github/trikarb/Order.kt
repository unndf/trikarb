package com.github.trikarb

import java.math.BigDecimal

abstract class Order(val rate: BigDecimal, val quantity: BigDecimal, val orderbook: Orderbook) {

}

class Bid(rate: BigDecimal, quantity: BigDecimal, orderbook: Orderbook): Order(rate, quantity, orderbook) {
}

class Ask(rate: BigDecimal, quantity: BigDecimal, orderbook: Orderbook): Order(rate, quantity, orderbook) {
}
