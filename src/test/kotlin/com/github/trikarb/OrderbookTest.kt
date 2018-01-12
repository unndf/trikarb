package com.github.trikarb

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP
import kotlin.test.*

class OrderbookTest
{
    val base = "A"
    val quote = "B"
    val scale = 9
    val ONE = BigDecimal.ONE.setScale(scale,ROUND_HALF_UP)
    val TWO = BigDecimal("2.0").setScale(scale,ROUND_HALF_UP)
    val THREE = BigDecimal("3.0").setScale(scale,ROUND_HALF_UP)
    val q1 = BigDecimal("13.37").setScale(scale,ROUND_HALF_UP)
    val q2 = BigDecimal("13.38").setScale(scale,ROUND_HALF_UP)
    val q3 = BigDecimal("13.39").setScale(scale,ROUND_HALF_UP)

    @Test fun `Add Bid BigDecimal`(){
        val book = Orderbook(base,quote,scale=scale)
        book.addBid(ONE,q1)
        book.addBid(TWO,q2)
        book.addBid(THREE,q3)
        
        assertTrue {book.bid.contains(ONE)}
        assertTrue {book.bid.contains(TWO)}
        assertTrue {book.bid.contains(THREE)}

        assertEquals(q1, book.bid[ONE])
        assertEquals(q2, book.bid[TWO])
        assertEquals(q3, book.bid[THREE])
    }

    @Test fun `Add Ask BigDecimal`(){
        val book = Orderbook(base,quote,scale=scale)
        book.addAsk(ONE,q1)
        book.addAsk(TWO,q2)
        book.addAsk(THREE,q3)
        
        assertTrue {book.ask.contains(ONE)}
        assertTrue {book.ask.contains(TWO)}
        assertTrue {book.ask.contains(THREE)}

        assertEquals(q1, book.ask[ONE])
        assertEquals(q2, book.ask[TWO])
        assertEquals(q3, book.ask[THREE])
    }

    @Test fun `Edit Bid BigDecimal`(){
        val book = Orderbook(base,quote,scale=scale)
        book.editBid(ONE,q1)
        book.editBid(TWO,q2)
        book.editBid(THREE,q3)
        
        assertTrue {book.bid.contains(ONE)}
        assertTrue {book.bid.contains(TWO)}
        assertTrue {book.bid.contains(THREE)}

        assertEquals(q1, book.bid[ONE])
        assertEquals(q2, book.bid[TWO])
        assertEquals(q3, book.bid[THREE])
    }

    @Test fun `Edit Ask BigDecimal`(){
        val book = Orderbook(base,quote,scale=scale)
        book.editAsk(ONE,q1)
        book.editAsk(TWO,q2)
        book.editAsk(THREE,q3)
        
        assertTrue {book.ask.contains(ONE)}
        assertTrue {book.ask.contains(TWO)}
        assertTrue {book.ask.contains(THREE)}

        assertEquals(q1, book.ask[ONE])
        assertEquals(q2, book.ask[TWO])
        assertEquals(q3, book.ask[THREE])
    }
    
    @Test fun `Remove Bid BigDecimal`(){
        val book = Orderbook(base,quote,scale=scale)
        book.addBid(ONE,q1)
        book.addBid(TWO,q2)
        book.addBid(THREE,q3)
        
        assertTrue {book.bid.contains(ONE)}
        assertTrue {book.bid.contains(TWO)}
        assertTrue {book.bid.contains(THREE)}

        assertEquals(q1, book.bid[ONE])
        assertEquals(q2, book.bid[TWO])
        assertEquals(q3, book.bid[THREE])

        book.removeBid(ONE)
        assertFalse {book.bid.contains(ONE)}

        book.removeBid(TWO)
        assertFalse {book.bid.contains(TWO)}

        book.removeBid(THREE)
        assertFalse {book.bid.contains(THREE)}
    }

    @Test fun `Remove Ask BigDecimal`(){
        val book = Orderbook(base,quote,scale=scale)
        book.addAsk(ONE,q1)
        book.addAsk(TWO,q2)
        book.addAsk(THREE,q3)
        
        assertTrue {book.ask.contains(ONE)}
        assertTrue {book.ask.contains(TWO)}
        assertTrue {book.ask.contains(THREE)}

        assertEquals(q1, book.ask[ONE])
        assertEquals(q2, book.ask[TWO])
        assertEquals(q3, book.ask[THREE])

        book.removeAsk(ONE)
        assertFalse {book.ask.contains(ONE)}

        book.removeAsk(TWO)
        assertFalse {book.ask.contains(TWO)}

        book.removeAsk(THREE)
        assertFalse {book.ask.contains(THREE)}
    }
}
