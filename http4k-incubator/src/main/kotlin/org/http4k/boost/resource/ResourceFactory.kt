package org.http4k.boost.resource

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map
import dev.forkhandles.values.ValueFactory
import org.http4k.boost.Http4kApplicationBuilder
import org.http4k.boost.Http4kError
import org.http4k.boost.Model
import org.http4k.boost.ModelId
import org.http4k.contract.bindContract
import org.http4k.contract.div
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.value

fun interface GetResource<Resource: Model<Id, IdPrim>, Id: ModelId<IdPrim>, IdPrim: Any>:
        (Request, Id) -> Result4k<Resource, Http4kError>

fun interface CreateResource<Resource: Model<Id, IdPrim>, Id: ModelId<IdPrim>, IdPrim: Any, Data: Any>:
        (Request, Data) -> Result4k<Resource, Http4kError>

fun interface UpdateResource<Resource: Model<Id, IdPrim>, Id: ModelId<IdPrim>, IdPrim: Any, Data: Any>:
        (Request, Id, Data) -> Result4k<Resource, Http4kError>

inline fun <reified M: Model<Id, IdPrim>, Id: ModelId<IdPrim>, IdPrim: Any, reified Data: Any> Http4kApplicationBuilder.resource2(
    path: String,
    idFactory: ValueFactory<Id, IdPrim>,
    get: GetResource<M, Id, IdPrim>? = null,
    create: CreateResource<M, Id, IdPrim, Data>? = null,
    update: UpdateResource<M, Id, IdPrim, Data>? = null,
//        list: Request.() -> Page<M, Id, IdPrim> = { Page.empty() },
) {
    val idLens = Path.value(idFactory).of("${path.trimEnd('s')}_id")
    val itemLens = lens<M>().toLens()
    val dataLens = lens<Data>().toLens()

    contractRoutes += buildList {
        if (get != null) {
            this += path / idLens bindContract Method.GET to { id ->
                { request ->
                    get(request, id)
                        .map { Response(OK).with(itemLens of it) }
                        .orError()
                }
            }
        }

        if (create != null) {
            this += path bindContract Method.POST to { request: Request ->
                create(request, dataLens(request))
                    .map { Response(CREATED).with(itemLens of it) }
                    .orError()
            }
        }

        if (update != null) {
            this += path / idLens bindContract Method.PUT to { id ->
                { request ->
                    update(request, id, dataLens(request))
                        .map { Response(OK).with(itemLens of it) }
                        .orError()
                }
            }
        }
    }
}

