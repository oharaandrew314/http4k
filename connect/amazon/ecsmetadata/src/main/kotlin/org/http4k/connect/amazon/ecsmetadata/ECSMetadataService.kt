package org.http4k.connect.amazon.ecsmetadata

import dev.forkhandles.result4k.Result
import org.http4k.connect.Http4kConnectApiClient
import org.http4k.connect.RemoteFailure

@Http4kConnectApiClient
interface ECSMetadataService {
    operator fun <R: Any> invoke(action: ECSMetadataAction<R>): Result<R, RemoteFailure>

    companion object
}