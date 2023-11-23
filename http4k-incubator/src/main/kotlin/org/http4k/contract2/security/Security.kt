package org.http4k.contract2.security

import org.http4k.core.Filter

interface Security {
    val filter: Filter

    companion object {
        val None: Security? = null
    }
}
