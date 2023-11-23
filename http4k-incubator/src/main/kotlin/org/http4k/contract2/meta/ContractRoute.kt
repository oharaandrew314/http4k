package org.http4k.contract2.meta

import org.http4k.core.Method
import org.http4k.lens.PathLens

data class ContractRoute(
    val path: String,
    val pathLenses: List<PathLens<*>>,
    val method: Method,
    val operation: RouteMeta
)
