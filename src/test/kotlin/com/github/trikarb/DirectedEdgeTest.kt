package com.github.trikarb

import com.github.trikarb.util.inverse
import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import kotlin.test.*


class DirectedEdgeTest {
    val A = "A"
    val B = "B"
    val scale = 9

    val bestAsk = BigDecimal("1.3").setScale(scale,ROUND_HALF_UP)
    val bestBid = BigDecimal("1.3").setScale(scale,ROUND_HALF_UP)

    val AB = Orderbook(A,B,scale=scale)
    
    init {
        AB.addBid(bestBid, BigDecimal.ONE)
        AB.addAsk(bestAsk, BigDecimal.ONE)
    }

    val ABEdge = DirectedEdge(AB)
    val BAEdge = DirectedEdge(AB,reverse=true)

    @Test fun `rate test`(){
        assertEquals (bestAsk.inverse(), ABEdge.rate())
        assertEquals (bestBid, BAEdge.rate())
        assertEquals (BigDecimal.ONE.setScale(scale,ROUND_HALF_UP), (ABEdge.rate() * BAEdge.rate()).setScale(scale,ROUND_HALF_UP))
    }
}
