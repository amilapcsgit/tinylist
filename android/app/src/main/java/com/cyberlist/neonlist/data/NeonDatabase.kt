package com.cyberlist.neonlist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [ListEntity::class, ItemEntity::class],
  version = 3,
  exportSchema = true
)
abstract class NeonDatabase : RoomDatabase() {
  abstract fun listDao(): ListDao
  abstract fun itemDao(): ItemDao

  companion object {
    @Volatile private var INSTANCE: NeonDatabase? = null
    private val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `items_new` (
            `id` TEXT NOT NULL,
            `listId` TEXT NOT NULL,
            `text` TEXT NOT NULL,
            `isDone` INTEGER NOT NULL,
            `color` TEXT NOT NULL,
            `createdAt` INTEGER NOT NULL,
            PRIMARY KEY(`id`),
            FOREIGN KEY(`listId`) REFERENCES `lists`(`id`) ON DELETE CASCADE
          )
          """.trimIndent()
        )
        db.execSQL(
          """
          INSERT INTO `items_new` (`id`, `listId`, `text`, `isDone`, `color`, `createdAt`)
          SELECT `id`, `listId`, `text`, `isDone`, `color`, `createdAt` FROM `items`
          """.trimIndent()
        )
        db.execSQL("DROP TABLE `items`")
        db.execSQL("ALTER TABLE `items_new` RENAME TO `items`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_listId` ON `items` (`listId`)")
      }
    }
    private val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `items` ADD COLUMN `sort_order` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE `items` SET `sort_order` = `createdAt`")
      }
    }

    fun getInstance(context: Context): NeonDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          NeonDatabase::class.java,
          "neonlist.db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
          .build().also { INSTANCE = it }
      }
    }
  }
}
