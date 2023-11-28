package org.http4k.boost

import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.random
import org.http4k.core.Status.Companion.NOT_FOUND
import java.util.UUID

class CatId(value: UUID): UUIDValue(value), ModelId<UUID> {
    companion object: UUIDValueFactory<CatId>(::CatId)
}

data class Cat(
    override val id: CatId,
    val name: String
): Model<CatId, UUID>

class CatService(vararg cats: Cat) {

    private val repo = cats.associateBy { it.id }.toMutableMap()

    fun create(data: CatData) = Cat(id = CatId.random(), name = data.name)
        .also { repo[it.id] = it }
        .let { Success(it) }

    operator fun get(id: CatId) = repo[id].asResultOr { catNotFound(id) }

    operator fun set(id: CatId, data: CatData) = repo[id]
        .asResultOr { catNotFound(id) }
        .map { it.copy(name = data.name) }
        .peek { repo[id] = it }
}

data class CatData(
    val name: String
)

fun catNotFound(id: CatId) = Http4kError(
    status = NOT_FOUND,
    message = "cat $id not found"
)
