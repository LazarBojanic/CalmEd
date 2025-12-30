package com.calmed.calmedfrontendtourettes.database

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.calmed.calmedfrontendtourettes.model.raw.MessageEntity
import com.calmed.calmedfrontendtourettes.repository.IMessageDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities =
	[MessageEntity::class],
	version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getMessageDao(): IMessageDao
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