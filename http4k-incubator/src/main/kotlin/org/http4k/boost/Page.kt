package org.http4k.boost

import org.http4k.lens.Query
import org.http4k.lens.QueryLens
import org.http4k.lens.int

data class Page<Type: Model<Id>, Id: ModelId>(
    val items: List<Type>,
    val next: Id?
) {
    companion object {
        fun <Type: Model<Id>, Id: ModelId> empty() = Page<Type, Id>(emptyList(), null)
    }
}

data class PageRequest<Id: ModelId>(
    val cursor: Id?,
    val size: UInt
) {
    companion object {
        val sizeLens: QueryLens<UInt> = Query.int()
            .map(Int::toUInt, UInt::toInt)
            .defaulted("page_size", 100u)
    }
}
