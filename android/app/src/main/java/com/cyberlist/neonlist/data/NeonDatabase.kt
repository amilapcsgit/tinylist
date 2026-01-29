package com.cyberlist.neonlist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [ListEntity::class, ItemEntity::class],
  version = 1,
  exportSchema = false
)
abstract class NeonDatabase : RoomDatabase() {
  abstract fun listDao(): ListDao
  abstract fun itemDao(): ItemDao

  companion object {
    @Volatile private var INSTANCE: NeonDatabase? = null

    fun getInstance(context: Context): NeonDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          NeonDatabase::class.java,
          "neonlist.db"
        ).build().also { INSTANCE = it }
      }
    }
  }
}
