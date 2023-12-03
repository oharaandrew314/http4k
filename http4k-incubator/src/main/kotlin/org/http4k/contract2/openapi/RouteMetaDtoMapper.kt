package org.http4k.contract2.openapi

import org.http4k.contract2.meta.RestApi
import org.http4k.contract2.meta.RouteMeta

//fun RestApi.toDtoV3() = OpenApiDto(
//    openapi = "3.1.0",
//    info = OpenApiInfoDto(
//        title = info.title,
//        description = info.description,
//        version = info.version,
//        termsOfService = null,
//        contact = null,
//        license = null,
//        summary = null
//    ),
//    jsonSchemaDialect = null,
//    servers = null,
//    paths = routes
//        .groupBy { it.path }
//        .map { operations ->
//
//        }
//)

/*
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
 */
