package com.calmed.calmedfrontendtourettes.database

import androidx.room.RoomDatabaseConstructor

public actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase {
        throw NotImplementedError("Room AppDatabase_Impl is not generated for iOS in this setup.")
    }
}