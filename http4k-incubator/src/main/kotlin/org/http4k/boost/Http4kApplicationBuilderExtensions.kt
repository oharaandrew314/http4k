package org.http4k.boost

import org.http4k.cloudnative.health.Health
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.openapi.v3.OpenApi3ApiRenderer
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.format.Json
import org.http4k.format.Moshi

fun <NODE: Any> Http4kApplicationBuilder.buildWithSwaggerUI(json: Json<NODE>): Http4kApplication {
    httpHandlers += swaggerUiLite {
        pageTitle = name
        url = "openapi.json"

        requestSnippetsEnabled = true
        displayOperationId = true
        deepLinking = true
    }
    return build {
        descriptionPath = "openapi.json"
        renderer = OpenApi3(
            apiInfo = ApiInfo(name, "1"),
            apiRenderer = OpenApi3ApiRenderer(json),
            json = json
        )
    }
}

fun Http4kApplicationBuilder.buildWithSwaggerUI() = buildWithSwaggerUI(Moshi)

fun Http4kApplicationBuilder.addHealthCheck() {
    httpHandlers += Health()
}

