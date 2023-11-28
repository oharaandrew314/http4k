package org.http4k.boost

import com.natpryce.hamkrest.assertion.assertThat
import dev.forkhandles.values.random
import org.http4k.boost.resource.resource
import org.http4k.boost.resource.resource2
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.hamkrest.hasBody
import org.http4k.hamkrest.hasStatus
import org.junit.jupiter.api.Test

class ResourceTest {

    private val toggles = Cat(CatId.random(), "Toggles")
    private val cats = CatService(toggles)

    @Test
    fun `top level resource with dsl builder`() {
        val app = http4kBoost("Cats Api") {
            addHealthCheck()
            resource<Cat, CatId>("cats", CatId) {
                get(cats::get)
                create<CatData>(cats::create)
                update<CatData>(cats::set)
            }
        }.build()

        val getResponse = Request(GET, "/cats/${toggles.id}").let(app)
        assertThat(getResponse, hasStatus(OK))
        assertThat(getResponse, hasBody("""{"id":"${toggles.id}","name":"Toggles"}"""))
    }

    @Test
    fun `top level resource with kwargs builder`() {
        val app = http4kBoost("Cats Api") {
            addHealthCheck()
            resource2<Cat, CatId, CatData>(
                path = "cats",
                idFactory = CatId,
                get = { _, id -> cats[id] },
                create = { _, data -> cats.create(data) },
                update = { _, id, data -> cats.set(id, data) }
            )
        }.build()

        val getResponse = Request(GET, "/cats/${toggles.id}").let(app)
        assertThat(getResponse, hasStatus(OK))
        assertThat(getResponse, hasBody("""{"id":"${toggles.id}","name":"Toggles"}"""))
    }
}

