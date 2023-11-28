package org.http4k.boost

import dev.forkhandles.values.Value

interface Model<Id: ModelId<PrimId>, PrimId: Any> {
    val id: Id
}

interface ModelId<Prim: Any>: Value<Prim>
