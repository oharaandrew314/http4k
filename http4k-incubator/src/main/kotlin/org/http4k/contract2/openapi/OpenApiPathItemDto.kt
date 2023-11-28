package org.http4k.contract2.openapi

data class OpenApiPathItemDto(
    val summary: String?,
    val description: String?,
    val get: OpenApiOperationDto?,
    val put: OpenApiOperationDto?,
    val post: OpenApiOperationDto?,
    val delete: OpenApiOperationDto?,
    val options: OpenApiOperationDto?,
    val head: OpenApiOperationDto?,
    val patch: OpenApiOperationDto?,
    val trace: OpenApiOperationDto?,
    val servers: List<OpenApiServerDto>?,
    val parameters: List<OpenApiReferencable<OpenApiParameterDto>>?
)

data class OpenApiOperationDto(
    val tags: String?, // comma-separated
    val sunmmary: String?,
    val description: String?,
    val externalDocs: OpenApiExternalDocsDto?,
    val operationId: String?,
    val parameters: OpenApiReferencable<List<OpenApiParameterDto>>?,
    val requestBody: OpenApiReferencable<OpenApiRequestBodyDto>?,
    val responses: Responses?,
    val callbacks: Map<String, OpenApiReferencable<OpenApiCallbackDto>>?,
    val deprecated: Boolean?,
    val security: List<SecurityRequirement>?,
    val servers: List<OpenApiServerDto>?
) {
    data class Responses(
        val default: OpenApiReferencable<OpenApiResponseDto>,
        val statusCodes: Map<String, OpenApiReferencable<OpenApiResponseDto>> // flattened
    )
}
