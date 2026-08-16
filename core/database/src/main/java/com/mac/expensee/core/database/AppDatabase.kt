package com.mac.expensee.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mac.expensee.core.database.dao.CategoryDao
import com.mac.expensee.core.database.dao.ExpenseDao
import com.mac.expensee.core.database.dao.UserDao
import com.mac.expensee.core.database.entity.CategoryEntity
import com.mac.expensee.core.database.entity.ExpenseEntity
import com.mac.expensee.core.database.entity.SyncStatusConverters
import com.mac.expensee.core.database.entity.UserEntity
import com.mac.expensee.core.database.seed.DefaultCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val DATABASE_NAME = "expensee.db"

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class, UserEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(SyncStatusConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao

    companion object {
        fun build(context: Context, applicationScope: CoroutineScope): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(
                    object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seeding needs DAO access, which requires the built instance; this callback
                            // fires before Room.databaseBuilder() returns, so it's dispatched onto the
                            // application scope rather than run synchronously here.
                        }
                    },
                )
                .build()
                .also { database -> seedDefaultCategoriesIfEmpty(database, applicationScope) }

        private fun seedDefaultCategoriesIfEmpty(database: AppDatabase, scope: CoroutineScope) {
            scope.launch {
                val dao = database.categoryDao()
                if (dao.count() == 0) {
                    dao.insertAll(DefaultCategories.seedEntities(System.currentTimeMillis()))
                }
            }
        }
    }
}
