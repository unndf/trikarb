package com.github.trikarb.util

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP

fun BigDecimal.inverse(): BigDecimal {
    val scale = this.scale()
    return (BigDecimal.ONE.setScale(scale, ROUND_HALF_UP) / this).setScale(scale, ROUND_HALF_UP)
}
