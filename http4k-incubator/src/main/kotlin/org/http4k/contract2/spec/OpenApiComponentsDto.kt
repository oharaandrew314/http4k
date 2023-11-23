package org.http4k.contract2.spec

import org.http4k.core.Uri

data class OpenApiComponentsDto(
    val schemas: Map<String, OpenApiSchemaDto>,
    val responses: Map<String, OpenApiReferencable<OpenApiResponseDto>>,
    val parameters: Map<String, OpenApiReferencable<OpenApiParameterDto>>?,
    val examples: Map<String, OpenApiReferencable<OpenApiExampleDto>>?,
    val requestBodies: Map<String, OpenApiReferencable<OpenApiRequestBodyDto>>?,
    val headers: Map<String, OpenApiReferencable<OpenApiHeaderDto>>?,
    val securitySchemes: Map<String, OpenApiReferencable<OpenApiSecuritySchemeDto>>?,
    val links: Map<String, OpenApiReferencable<OpenApiLinkDto>>?,
    val callbacks: Map<String, OpenApiReferencable<OpenApiCallbackDto>>,
    val pathItems: Map<String, OpenApiReferencable<OpenApiPathItemDto>>
)

data class OpenApiSchemaDto(
    val discriminator: Discriminator?,
    val xml: Xml?,
    val externalDocs: OpenApiExternalDocsDto?,
    val example: Any?  // deprecated in favor of json schema examples keyword
): OpenApiReferencable<OpenApiSchemaDto> {
    data class Discriminator(
        val propertyName: String,
        val mapping: Map<String, String>?,
        val externalDocs: OpenApiExternalDocsDto?,
        val example: Any? // deprecated in favor of json schema examples keyword
    )
    data class Xml(
        val name: String?,
        val namespace: Uri?,
        val prefix: String?,
        val attribute: Boolean,
        val wrapped: Boolean
    )
}

data class OpenApiResponseDto(
    val description: String,
    val headers: Map<String, OpenApiReferencable<OpenApiHeaderDto>>,
    val content: Map<String, OpenApiMediaTypeDto>?,
    val links: Map<String, OpenApiReferencable<OpenApiLinkDto>>,
): OpenApiReferencable<OpenApiResponseDto>

data class OpenApiLinkDto(
    val operationRef: Uri?, // mutually exclusive with operationId
    val operationId: String?, // mutually exclusive with operationRef
    val parameters: Map<String, Any>?,
    val requestBody: Any?,
    val description: String?,
    val server: OpenApiServerDto?
)

data class OpenApiParameterDto(
    val name: String,
    val `in`: Location,
    val description: String?,
    val required: Boolean?,
    val deprecated: Boolean?,
    val allowEmptyValue: Boolean? // deprecated
): OpenApiReferencable<OpenApiParameterDto> {
    enum class Location { query, header, path, cookie }
}


data class OpenApiRequestBodyDto(
    val description: String?,
    val content: Map<String, OpenApiMediaTypeDto>,
    val required: Boolean?,
)

sealed interface OpenApiSecuritySchemeDto {
    val type: Type
    val description: String?
//    val name: String, // applied to apiKey type

    enum class Type { apiKey, http, mutualTLS, oauth2, openIdConnect }

    data class ApiKey(
        override val description: String?,
        val name: String,
        val `in`: Location,
    ): OpenApiSecuritySchemeDto {
        override val type = Type.apiKey

        enum class Location { query, headers, cookie }
    }

    data class Http(
        override val description: String?,
        val scheme: String,
        val bearerFormat: String?
    ): OpenApiSecuritySchemeDto {
        override val type = Type.http
    }

    data class MutualTls(
        override val description: String?
    ): OpenApiSecuritySchemeDto {
        override val type = Type.mutualTLS
    }

    data class Oauth2(
        override val description: String?,
        val flows: Flows?
    ): OpenApiSecuritySchemeDto {
        override val type = Type.oauth2

        data class Flows(
            val implicit: Flow.Implicit?,
            val password: Flow.Password?,
            val clientCredentials: Flow.ClientCredentials?,
            val authorizationCode: Flow.AuthorizationCode?
        )

        sealed interface Flow {
            val refreshUrl: Uri?
            val scopes: Map<String, String>

            data class Implicit(
                val authorizationUrl: Uri,
                override val refreshUrl: Uri?,
                override val scopes: Map<String, String>
            ): Flow

            data class AuthorizationCode(
                val authorizationUrl: Uri, // applies to implicit, authCode
                val tokenUrl: Uri,
                override val refreshUrl: Uri?,
                override val scopes: Map<String, String>
            ): Flow

            data class ClientCredentials(
                val tokenUrl: Uri,
                override val refreshUrl: Uri?,
                override val scopes: Map<String, String>
            ): Flow

            data class Password(
                override val refreshUrl: Uri?,
                override val scopes: Map<String, String>
            ): Flow
        }
    }

    data class OpenIdConnect(
        override val description: String?,
        val openIdConnectUrl: Uri
    ): OpenApiSecuritySchemeDto {
        override val type = Type.openIdConnect
    }
}

/**
 * {expression}:
 *  post:
 *   ...
 */
typealias OpenApiCallbackDto = Map<String, OpenApiReferencable<OpenApiPathItemDto>>
