package org.http4k.boost

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.recover
import dev.forkhandles.time.TimeSource
import dev.forkhandles.time.systemTime
import org.http4k.client.JavaHttpClient
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.cloudnative.env.Port
import org.http4k.contract.ContractBuilder
import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.format.AutoMarshalling
import org.http4k.format.Moshi
import org.http4k.lens.ContentNegotiation
import org.http4k.lens.PathLens
import org.http4k.lens.port
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes
import org.http4k.server.ServerConfig
import org.http4k.server.Undertow
import org.http4k.util.Appendable

object ServerEnv {
    val port = EnvironmentKey.port().defaulted("SERVICE_PORT", Port(8000))
}

fun http4kBoost(
    name: String = "Http4k Application",
    env: Environment = Environment.ENV,
    autoMarshalling: AutoMarshalling = Moshi,
    internet: HttpHandler = JavaHttpClient(),
    time: TimeSource = systemTime,
    fn: Http4kApplicationBuilder.() -> Unit
) = Http4kApplicationBuilder(
    name = name,
    env = env,
    autoMarshalling = autoMarshalling,
    internet = internet,
    time = time
).also(fn)

class Http4kApplicationBuilder(
    val name: String,
    val env: Environment,
    val autoMarshalling: AutoMarshalling,
    val internet: HttpHandler,
    val time: TimeSource
) {
    val idLenses = Appendable<PathLens<Any>>()
    val contractRoutes = Appendable<ContractRoute>()
    val httpHandlers = Appendable<RoutingHttpHandler>()

    inline fun <reified T: Any> lens() = Body
        .string(autoMarshalling.defaultContentType, null, ContentNegotiation.None)
        .map({ autoMarshalling.asA<T>(it) }, { autoMarshalling.asFormatString(it) })

    fun build(
        server: ServerConfig = Undertow(env[ServerEnv.port].value),
        fn: ContractBuilder.() -> Unit = {},
    ) = Http4kApplication(
        app = routes(
            contract {
                routes += this@Http4kApplicationBuilder.contractRoutes.all
                let(fn)
            },
            *httpHandlers.all.toTypedArray()
        ),
        server = server
    )

    fun Result4k<Response, Http4kError>.orError() = recover {
        Response(it.status).body(autoMarshalling.asFormatString(it))
    }

    operator fun invoke(fn: Http4kApplicationBuilder.() -> Unit): HttpHandler =
        apply(fn).build()
}
