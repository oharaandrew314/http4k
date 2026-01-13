package org.http4k.connect.amazon.ecsmetadata

import dev.forkhandles.result4k.Success
import org.http4k.config.Environment
import org.http4k.connect.amazon.ecsmetadata.actions.GetContainerMetadata
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class GetContainerMetadataTest {

    private val env = Environment.defaults(
        ECS_CONTAINER_METADATA_URI_V4 of Uri.of("http://localhost")
    )

    private val meta = fakeEcsTaskMetadata(Instant.parse("2025-01-01T12:00:00Z"))
    private val fake = FakeECSMetadataService(meta)

    @Test
    fun `sdk request succeeds`() {
        val client = fake.client()
        assertEquals(
            Success(meta.Containers.first()),
            client(GetContainerMetadata())
        )
    }

    @Test
    fun `request with trailing slash fails`() {
        assertEquals(
            Response(Status.NOT_FOUND),
            Request(Method.GET, "http://localhost/").let(fake)
        )
    }

    @Test
    fun `request without trailing slash succeeds`() {
        assertEquals(
            with(ECSMetadataServiceMoshi) {
                Response(Status.OK).json(meta.Containers.first())
            },
            Request(Method.GET, "http://localhost").let(fake)
        )
    }
}