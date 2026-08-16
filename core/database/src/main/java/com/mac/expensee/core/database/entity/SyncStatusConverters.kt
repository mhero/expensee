package com.mac.expensee.core.database.entity

import androidx.room.TypeConverter
import com.mac.expensee.core.common.sync.SyncStatus

/**
 * Room stores [SyncStatus] as its enum name (TEXT). A dedicated converter class (rather than
 * relying on Room's implicit enum support) keeps the on-disk representation explicit and stable
 * even if enum ordinal order changes.
 */
class SyncStatusConverters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
