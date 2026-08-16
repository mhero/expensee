package com.mac.expensee.core.common.sync

/**
 * Lifecycle of a locally-owned, syncable record with respect to a (future) remote API.
 * Lives in `core:common` -- rather than `core:database` -- because both the data layer
 * (Room entities) and domain layer (use cases deciding what still needs to sync) need it,
 * and neither should depend on the other's persistence details.
 */
enum class SyncStatus {
    /** Created/modified locally, not yet pushed to the remote API. */
    PENDING_UPLOAD,

    /** In sync with the remote API as of `updatedAt`. */
    SYNCED,

    /** Deleted locally; tombstoned until the deletion is confirmed remotely. */
    PENDING_DELETE,

    /** The last sync attempt failed (e.g. conflict, validation error server-side). */
    SYNC_FAILED,
}
