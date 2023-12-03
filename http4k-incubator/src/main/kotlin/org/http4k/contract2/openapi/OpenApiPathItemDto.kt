package org.http4k.contract2.openapi

data class OpenApiPathItemDto(
    val summary: String? = null,
    val description: String? = null,
    val get: OpenApiOperationDto? = null,
    val put: OpenApiOperationDto? = null,
    val post: OpenApiOperationDto? = null,
    val delete: OpenApiOperationDto? = null,
    val options: OpenApiOperationDto? = null,
    val head: OpenApiOperationDto? = null,
    val patch: OpenApiOperationDto? = null,
    val trace: OpenApiOperationDto? = null,
    val servers: List<OpenApiServerDto>? = null,
    val parameters: List<OpenApiParameterDto>? = null
)

data class OpenApiOperationDto(
    val tags: List<String>? = null,
    val summary: String? = null,
    val description: String? = null,
    val externalDocs: OpenApiExternalDocsDto? = null,
    val operationId: String? = null,
    val parameters: List<OpenApiParameterDto>? = null, // TODO should be referencable
    val requestBody: OpenApiRequestBodyDto? = null,
    val responses: Map<String, OpenApiResponseDto>? = null,
    val callbacks: Map<String, OpenApiCallbackDto>? = null,
    val deprecated: Boolean? = null,
    val security: List<SecurityRequirement>? = null,
    val servers: List<OpenApiServerDto>? = null
)
