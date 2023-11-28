package org.http4k.boost

import dev.forkhandles.values.Value

interface Model<Id: ModelId> {
    val id: Id
}

typealias ModelId = Value<out Any>
