package org.http4k.contract2.meta

import org.http4k.contract2.security.Security
import org.http4k.core.ContentType
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.BodyLens
import org.http4k.lens.Lens

data class RouteMeta(
    val path: String,

    val operationId: String?,
    val summary: String?,
    val description: String?,
    val tags: Set<Tag> = emptySet(),

    val deprecated: Boolean,
    val described: Boolean,

    val body: BodyLens<*>?,
    val produces: Set<ContentType>,
    val consumes: Set<ContentType>,
    val requestParams: List<Lens<Request, *>>,
    val requests: List<HttpMessageMeta<Request>>,
    val responses: List<HttpMessageMeta<Response>>,
    val preFlightExtraction: PreFlightExtraction,

    val security: Security?,
)

data class Tag(val name: String, val description: String? = null)

class RequestMeta(request: Request, definitionId: String? = null, example: Any? = null, schemaPrefix: String? = null) :
    HttpMessageMeta<Request>(request, "request", definitionId, example, schemaPrefix)

class ResponseMeta(
    description: String,
    response: Response,
    definitionId: String? = null,
    example: Any? = null,
    schemaPrefix: String? = null
) : HttpMessageMeta<Response>(response, description, definitionId, example, schemaPrefix)
