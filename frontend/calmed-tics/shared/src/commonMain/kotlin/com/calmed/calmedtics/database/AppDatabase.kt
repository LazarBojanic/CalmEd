package com.calmed.calmedtics.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.calmed.calmedtics.model.RoomConverters
import com.calmed.calmedtics.model.raw.UserEntity
import com.calmed.calmedtics.model.raw.UserInfoTicsEntity
import com.calmed.calmedtics.model.raw.ExerciseCompletionEntity
import com.calmed.calmedtics.repository.IUserDao
import com.calmed.calmedtics.repository.IUserInfoTicsDao
import com.calmed.calmedtics.repository.IExerciseCompletionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(version = 4, entities = [UserEntity::class, UserInfoTicsEntity::class, ExerciseCompletionEntity::class])
@TypeConverters(RoomConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getUserDao(): IUserDao
	abstract fun getUserInfoTicsDao(): IUserInfoTicsDao
	abstract fun getExerciseCompletionDao(): IExerciseCompletionDao
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
