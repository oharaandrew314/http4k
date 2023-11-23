package org.http4k.contract2.meta

import org.http4k.core.HttpMessage

open class HttpMessageMeta<out T : HttpMessage>(
    val message: T,
    val description: String,
    val definitionId: String?,
    val example: Any?,
    val schemaPrefix: String? = null
)
