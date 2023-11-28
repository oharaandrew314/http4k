package org.http4k.contract2.meta

data class RestApi(
    val info: ApiInfo,
    val routes: List<ContractRoute>
)
