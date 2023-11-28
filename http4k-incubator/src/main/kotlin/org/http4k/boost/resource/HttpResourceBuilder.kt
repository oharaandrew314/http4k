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
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Path
import org.http4k.lens.PathLens

class HttpResourceBuilder<Resource: Any, Id: Any>(
    val appBuilder: Http4kApplicationBuilder,
    val path: String,
    val idLens: PathLens<Id>,
    val itemLens: BiDiBodyLens<Resource>,
) {
    fun get(
        fn: (Id) -> Result4k<Resource, Http4kError>
    ) = with(appBuilder) {
        contractRoutes += path / idLens bindContract org.http4k.core.Method.GET to { id ->
            {
                fn(id)
                    .map { Response(OK).with(itemLens of it) }
                    .orError()
            }
        }
    }

    inline fun <reified CreateData: Any> create(
        crossinline fn: (CreateData) -> Result4k<Resource, Http4kError>
    ) = with(appBuilder) {
        val dataLens = appBuilder.lens<CreateData>().toLens()
        path bindContract org.http4k.core.Method.POST to { request: Request ->
            fn(dataLens(request))
                .map { Response(CREATED).with(itemLens of it) }
                .orError()
        }
    }

    inline fun <reified UpdateData: Any> update(
        crossinline fn: (Id, UpdateData) -> Result4k<Resource, Http4kError>
    ) = with(appBuilder) {
        val dataLens = appBuilder.lens<UpdateData>().toLens()
        path / idLens bindContract org.http4k.core.Method.PUT to { id ->
            { request ->
                fn(id, dataLens(request))
                    .map { Response(OK).with(itemLens of it) }
                    .orError()
            }
        }
    }
}

inline fun <reified Resource: Model<Id>, Id: ModelId> Http4kApplicationBuilder.resource(
    path: String,
    idFactory: ValueFactory<Id, out Any>,
    fn: HttpResourceBuilder<Resource, Id>.() -> Unit
) {
    val idLens = Path.map(idFactory::parse).of("${path.trimEnd('s')}_id")
    HttpResourceBuilder(
        appBuilder = this,
        path = path,
        itemLens = lens<Resource>().toLens(),
        idLens = idLens
    ).apply(fn)
}
