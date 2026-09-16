package com.emarsys.core.util

inline fun <T1: Any, T2: Any, R> Pair<T1?, T2?>.safeLet(block: (T1, T2) -> R): R? {
    val currentFirst = this.first
    val currentSecond = this.second
    return if (currentFirst != null && currentSecond != null) {
        block(currentFirst, currentSecond)
    } else null
}

inline fun <T1: Any, T2: Any, T3: Any, R> Triple<T1?, T2?, T3?>.safeLet(block: (T1, T2, T3) -> R): R? {
    val currentFirst = this.first
    val currentSecond = this.second
    val currentThird = this.third
    return if (currentFirst != null && currentSecond != null && currentThird != null) {
        block(currentFirst, currentSecond, currentThird)
    } else null
}
