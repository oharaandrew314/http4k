package org.http4k.contract2.meta

data class OpenApi(
    val info: ApiInfo,
    val routes: List<ContractRoute>
)
