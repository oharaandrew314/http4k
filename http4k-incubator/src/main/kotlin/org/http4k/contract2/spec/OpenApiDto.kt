package org.http4k.contract2.spec

import org.http4k.core.Uri

data class OpenApiDto(
    val openapi: String,
    val info: OpenApiInfoDto,
    val jsonSchemaDialect: Uri?,
    val servers: List<OpenApiServerDto>?,
    val paths: Map<String, OpenApiPathItemDto>?,
    val webhooks: Map<String, OpenApiPathItemDto>?,
    val components: OpenApiComponentsDto?,
    val security: List<SecurityRequirement>?,
    val tags: List<OpenApiTagDto>?,
    val externalDocs: OpenApiExternalDocsDto?
)

data class OpenApiTagDto(
    val name: String,
    val description: String?,
    val externalDocs: OpenApiExternalDocsDto?
)

data class OpenApiInfoDto(
    val title: String,
    val summary: String?,
    val description: String?,
    val termsOfService: Uri?,
    val contact: OpenApiContactDto?,
    val license: OpenApiLicenseDto?,
    val version: String
)

data class OpenApiContactDto(
    val name: String?,
    val url: Uri?,
    val email: String?
)

// identifier and url are mutually exclusive
data class OpenApiLicenseDto(
    val name: String,
    val identifier: String?,
    val url: Uri?,
)

data class OpenApiServerDto(
    val url: Uri,
    val description: Uri?,
    val variables: Map<String, OpenApiServerVariableDto>
)

data class OpenApiServerVariableDto(
    val enum: List<String>?, // must not be empty
    val default: String,
    val description: String?
)

sealed interface OpenApiReferencable<T: Any>

data class OpenApiNamedReferenceDto<T: Any>(
    val `$ref`: String,
    val summary: String?,
    val description: String?,
): OpenApiReferencable<T>

data class OpenApiMediaTypeDto(
    val schema: OpenApiSchemaDto?,
    val example: Any?, // mutually exclusive with examples
    val examples: Map<String, OpenApiReferencable<OpenApiExampleDto>>?, // mutually exclusive with example
    val encoding: Map<String, Encoding>
) {
    data class Encoding(
        val contentType: String?, // single or comma-separated
        val headers: Map<String, OpenApiReferencable<OpenApiHeaderDto>>,
        val style: String?,
        val explode: Boolean?,
        val allowReserved: Boolean
    )
}

data class OpenApiHeaderDto(
    val name: String,
    val description: String?,
    val externalDocs: OpenApiExternalDocsDto?
)

data class OpenApiExternalDocsDto(
    val description: String,
    val url: Uri?
)

data class OpenApiExampleDto(
    val summary: String?,
    val description: String?,
    val value: Any?, // mutually exclusive with externalValue
    val externalValue: Uri?, // mutually exclusive with value
)

typealias SecurityRequirement = Map<String, List<String>>
