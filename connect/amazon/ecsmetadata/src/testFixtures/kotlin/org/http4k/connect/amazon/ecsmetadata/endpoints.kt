package org.http4k.connect.amazon.ecsmetadata

import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerMetadata
import org.http4k.connect.amazon.ecsmetadata.model.ECSTaskMetadata
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status

internal fun getMetadata(metadata: ECSTaskMetadata): HttpHandler = {
    with(ECSMetadataServiceMoshi) {
        Response(Status.OK).json(metadata)
    }
}

internal fun getContainerMetadata(metadata: ECSContainerMetadata): HttpHandler = {
    with(ECSMetadataServiceMoshi) {
        Response(Status.OK).json(metadata)
    }
}