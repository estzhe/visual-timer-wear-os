package com.estzhe.timer

data class Timers(
    val active: Timer?,
    val popular: Map<Int, Int>)