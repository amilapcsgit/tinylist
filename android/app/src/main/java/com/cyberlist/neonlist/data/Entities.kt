package com.cyberlist.neonlist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lists")
data class ListEntity(
  @PrimaryKey val id: String,
  val title: String,
  val color: String,
  val createdAt: Long,
  @ColumnInfo(name = "sort_order") val order: Int
)

@Entity(
  tableName = "items",
  foreignKeys = [
    ForeignKey(
      entity = ListEntity::class,
      parentColumns = ["id"],
      childColumns = ["listId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["listId"])]
)
data class ItemEntity(
  @PrimaryKey val id: String,
  val listId: String,
  val text: String,
  val isDone: Boolean,
  val color: String,
  val createdAt: Long,
  @ColumnInfo(name = "sort_order") val order: Long
)
