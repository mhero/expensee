package com.mac.expensee.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mac.expensee.core.database.AppDatabase
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Common in-memory-database scaffolding for every DAO test. A fresh, empty database is built for
 * each test method (via JUnit's per-method instantiation) and closed afterwards, so tests never
 * see state left over by another test. Deliberately doesn't go through [AppDatabase.build] --
 * that seeds default categories and takes a [kotlinx.coroutines.CoroutineScope], neither of which
 * a DAO test wants; `allowMainThreadQueries()` is used instead since Robolectric runs tests on
 * the same thread Room would otherwise reject as "main".
 */
@RunWith(RobolectricTestRunner::class)
abstract class RoomDaoTest {

    protected lateinit var db: AppDatabase

    @Before
    fun setUpDatabase() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        db.close()
    }
}
