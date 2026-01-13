package org.http4k.connect.amazon.ecsmetadata

import org.http4k.client.JavaHttpClient
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.lens.uri

fun ECSMetadataService.Companion.Http(
    environment: Environment = Environment.ENV,
    http: HttpHandler = JavaHttpClient()
) = object : ECSMetadataService {
    private val authorizedHttp = SetBaseUriFrom(environment[ECS_CONTAINER_METADATA_URI_V4])
        .then(ClientFilters.SetXForwardedHost())
        .then(http)

    override fun <R: Any> invoke(action: ECSMetadataAction<R>) =
        action.toResult(authorizedHttp(action.toRequest()))
}

val ECS_CONTAINER_METADATA_URI_V4 = EnvironmentKey.uri().required("ECS_CONTAINER_METADATA_URI_V4")