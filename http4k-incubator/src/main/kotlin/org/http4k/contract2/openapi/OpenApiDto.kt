package org.http4k.contract2.openapi

import org.http4k.core.Uri

data class OpenApiDto(
    val openapi: String,
    val info: OpenApiInfoDto,
    val jsonSchemaDialect: Uri? = null,
    val servers: List<OpenApiServerDto>? = null,
    val paths: Map<String, OpenApiPathItemDto>? = null,
    val webhooks: Map<String, OpenApiPathItemDto>? = null,
    val components: OpenApiComponentsDto? = null,
    val security: List<SecurityRequirement>? = null,
    val tags: List<OpenApiTagDto>? = null,
    val externalDocs: OpenApiExternalDocsDto? = null
)

data class OpenApiTagDto(
    val name: String,
    val description: String? = null,
    val externalDocs: OpenApiExternalDocsDto? = null
)

data class OpenApiInfoDto(
    val title: String,
    val summary: String? = null,
    val description: String? = null,
    val termsOfService: Uri? = null,
    val contact: OpenApiContactDto? = null,
    val license: OpenApiLicenseDto? = null,
    val version: String
)

data class OpenApiContactDto(
    val name: String? = null,
    val url: Uri? = null,
    val email: String? = null
)

// identifier and url are mutually exclusive
data class OpenApiLicenseDto(
    val name: String,
    val identifier: String? = null,
    val url: Uri? = null,
)

data class OpenApiServerDto(
    val url: Uri,
    val description: Uri? = null,
    val variables: Map<String, OpenApiServerVariableDto>? = null
)

data class OpenApiServerVariableDto(
    val enum: List<String>? = null, // must not be empty
    val default: String,
    val description: String
)

data class OpenApiMediaTypeDto(
    val schema: OpenApiSchemaDto? = null,
    val example: Any? = null, // mutually exclusive with examples
    val examples: Map<String, OpenApiExampleDto>? = null, // mutually exclusive with example
    val encoding: Map<String, Encoding>? = null
) {
    data class Encoding(
        val contentType: String? = null, // single or comma-separated
        val headers: Map<String, OpenApiHeaderDto>,
        val style: String? = null,
        val explode: Boolean? = null,
        val allowReserved: Boolean
    )
}

data class OpenApiHeaderDto(
    val `$ref`: String? = null,
    val name: String? = null,
    val description: String? = null,
    val externalDocs: OpenApiExternalDocsDto? = null,
    val schema: OpenApiSchemaDto? = null
)

data class OpenApiExternalDocsDto(
    val description: String,
    val url: Uri? = null
)

data class OpenApiExampleDto(
    val summary: String? = null,
    val description: String? = null,
    val value: Any? = null, // mutually exclusive with externalValue
    val externalValue: Uri? = null, // mutually exclusive with value
)

typealias SecurityRequirement = Map<String, List<String>>
