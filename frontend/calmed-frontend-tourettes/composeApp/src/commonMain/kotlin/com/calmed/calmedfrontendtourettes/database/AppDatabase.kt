package com.calmed.calmedfrontendtourettes.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.calmed.calmedfrontendtourettes.model.RoomConverters
import com.calmed.calmedfrontendtourettes.model.raw.UserEntity
import com.calmed.calmedfrontendtourettes.model.raw.UserInfoTourettesEntity
import com.calmed.calmedfrontendtourettes.repository.IUserDao
import com.calmed.calmedfrontendtourettes.repository.IUserInfoTourettesDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(version = 1, entities = [UserEntity::class, UserInfoTourettesEntity::class])
@TypeConverters(RoomConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getUserDao(): IUserDao
	abstract fun getUserInfoTourettesDao(): IUserInfoTourettesDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
	override fun initialize(): AppDatabase
}

fun getAppDatabase(
	builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
	return builder
		.fallbackToDestructiveMigration(
			dropAllTables = true
		)
		.setDriver(BundledSQLiteDriver())
		.setQueryCoroutineContext(Dispatchers.IO)
		.build()
}
