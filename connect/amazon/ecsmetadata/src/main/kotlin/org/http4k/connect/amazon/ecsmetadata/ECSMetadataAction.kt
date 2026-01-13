package org.http4k.connect.amazon.ecsmetadata

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.connect.Action
import org.http4k.connect.RemoteFailure
import org.http4k.connect.asRemoteFailure
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import kotlin.reflect.KClass

abstract class ECSMetadataAction<R: Any>(private val path: String, private val clazz: KClass<R>) : Action<Result<R, RemoteFailure>> {
    override fun toRequest() = Request(Method.GET, path)

    override fun toResult(response: Response) = with(response) {
        when {
            status.successful -> Success(
                ECSMetadataServiceMoshi.asA(bodyString(), clazz)
            )

            else -> Failure(asRemoteFailure(this))
        }
    }
}