package org.http4k.contract2.meta

import org.http4k.core.Request
import org.http4k.lens.BodyLens
import org.http4k.lens.LensExtractor

fun interface PreFlightExtraction : (RouteMeta) -> List<LensExtractor<Request, *>> {
    companion object {

        /**
         * Check the entire contract, including extracting the body, before passing it to the underlying
         * HttpHandler.
         */
        val All = PreFlightExtraction {
            it.requestParams + (it.body?.let { listOf(it) }
                ?: emptyList<BodyLens<*>>())
        }

        /**
         * Check all parts of the contract apart from the body, relying on the HttpHandler code to raise a correct
         * LensFailure if extraction fails. Use this option to avoid re-extracting the body multiple times.
         */
        val IgnoreBody = PreFlightExtraction { it.requestParams }

        /**
         * Check none the contract, relying entirely  on the HttpHandler code to raise a correct
         * LensFailure if extraction fails. Use this option to fully optimise performance, at the risk
         * of not checking
         */
        val None = PreFlightExtraction { emptyList() }
    }
}
