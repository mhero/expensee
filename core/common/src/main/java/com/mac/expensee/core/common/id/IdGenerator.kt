package com.mac.expensee.core.common.id

import java.util.UUID

/** Generates client-side unique ids for entities created offline, before a server assigns one. */
interface IdGenerator {
    fun newId(): String
}

class UuidIdGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
