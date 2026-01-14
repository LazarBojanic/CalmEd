package com.calmed.calmedfrontendtourettes.database

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.calmed.calmedfrontendtourettes.model.raw.DummyEntity
import com.calmed.calmedfrontendtourettes.repository.IDummyDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(version = 1, entities = [DummyEntity::class])
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getDummyDao(): IDummyDao
}

@Suppress("KotlinNoActualForExpect") expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
	override fun initialize(): AppDatabase
}

fun getAppDatabase(
	builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
	return builder
		.setDriver(BundledSQLiteDriver())
		.setQueryCoroutineContext(Dispatchers.IO)
		.build()
}