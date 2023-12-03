package org.http4k.contract2.openapi

import org.http4k.core.Uri

data class OpenApiComponentsDto(
    val schemas: Map<String, OpenApiSchemaDto>? = null,
    val responses: Map<String, OpenApiResponseDto>? = null,
    val parameters: Map<String, OpenApiParameterDto>? = null,
    val examples: Map<String, OpenApiExampleDto>? = null,
    val requestBodies: Map<String, OpenApiRequestBodyDto>? = null,
    val headers: Map<String, OpenApiHeaderDto>? = null,
    val securitySchemes: Map<String, OpenApiSecuritySchemeDto>? = null,
    val links: Map<String, OpenApiLinkDto>? = null,
    val callbacks: Map<String, OpenApiCallbackDto>? = null,
    val pathItems: Map<String, OpenApiPathItemDto>? = null
)

data class OpenApiSchemaDto(
    val type: String? = null, // only nullable if ref
    val format: String? = null,
    val description: String? = null,
    val default: Any? = null,
    val required: List<String>? = null,
    val `$ref`: String? = null,

    // string
    val enum: List<String>? = null,

    // object
    val items: OpenApiSchemaDto? = null,
    val properties: Map<String, OpenApiSchemaDto>? = null,
    val additionalProperties: OpenApiSchemaDto? = null,

    val discriminator: Discriminator? = null,
    val xml: Xml? = null,
    val externalDocs: OpenApiExternalDocsDto? = null,
    val example: Any? = null,  // deprecated in favor of json schema examples keyword
) {
    data class Discriminator(
        val propertyName: String,
        val mapping: Map<String, String>? = null,
        val externalDocs: OpenApiExternalDocsDto? = null,
        val example: Any? = null // deprecated in favor of json schema examples keyword
    )
    data class Xml(
        val name: String? = null,
        val namespace: Uri? = null,
        val prefix: String? = null,
        val attribute: Boolean? = null,
        val wrapped: Boolean? = null
    )
}

data class OpenApiResponseDto(
    val description: String? = null, // only nullable if ref
    val headers: Map<String, OpenApiHeaderDto>? = null,
    val content: Map<String, OpenApiMediaTypeDto>? = null,
    val links: Map<String, OpenApiLinkDto>? = null,
    val `$ref`: String? = null,
)

data class OpenApiLinkDto(
    val operationRef: Uri? = null, // mutually exclusive with operationId
    val operationId: String? = null, // mutually exclusive with operationRef
    val parameters: Map<String, Any>? = null,
    val requestBody: Any? = null,
    val description: String? = null,
    val server: OpenApiServerDto? = null
)

data class OpenApiParameterDto(
    val `$ref`: String? = null,
    val name: String? = null, // only nullable if ref
    val `in`: Location? = null, // only nullable if ref
    val description: String? = null,
    val required: Boolean? = null,
    val explode: Boolean? = null,
    val schema: OpenApiSchemaDto? = null,
    val deprecated: Boolean? = null,
    val allowEmptyValue: Boolean? = null // deprecated
) {
    enum class Location { query, header, path, cookie }
}


data class OpenApiRequestBodyDto(
    val `$ref`: String? = null,
    val description: String? = null,
    val content: Map<String, OpenApiMediaTypeDto>,
    val required: Boolean? = null,
)

data class OpenApiSecuritySchemeDto(
    val `$ref`: String? = null,
    val type: Type? = null, // only nullable if ref
    val description: String? = null,

    // apiKey
    val name: String? = null,
    val `in`: Location? = null,

    // http
    val scheme: String? = null, // required if http
    val bearerFormat: String? = null,

    // oauth2
    val flows: Flows? = null,

    // OIDC
    val openIdConnectUrl: Uri? = null // required if OIDC

) {
    enum class Location { query, header, cookie }
    enum class Type { apiKey, http, mutualTLS, oauth2, openIdConnect }


    data class Flows(
        val implicit: Flow? = null,
        val password: Flow? = null,
        val clientCredentials: Flow? = null,
        val authorizationCode: Flow? = null
    )

    data class Flow(
        val refreshUrl: Uri? = null,
        val scopes: Map<String, String>? = null,
        val authorizationUrl: Uri? = null, // required for implicit, authCode
        val tokenUrl: Uri? = null, // required for authCode, clientCredentials
    )
}

/**
 * {expression}:
 *  post:
 *   ...
 */
typealias OpenApiCallbackDto = Map<String, OpenApiPathItemDto>
