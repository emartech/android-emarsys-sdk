package com.emarsys.core.util.predicate

class ListSizeAtLeast<T>(private val count: Int) : Predicate<List<T>> {
    override fun evaluate(input: List<T>): Boolean = input.size >= count
}
