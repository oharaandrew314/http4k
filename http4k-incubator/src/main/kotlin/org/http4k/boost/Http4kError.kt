package org.http4k.boost

import org.http4k.core.Status

data class Http4kError(
    val status: Status,
    val message: String = status.description,
    val details: Any? = null
)
