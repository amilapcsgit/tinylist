package com.cyberlist.neonlist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lists")
data class ListEntity(
  @PrimaryKey val id: String,
  val title: String,
  val color: String,
  val createdAt: Long,
  @ColumnInfo(name = "sort_order") val order: Int
)

@Entity(tableName = "items")
data class ItemEntity(
  @PrimaryKey val id: String,
  val listId: String,
  val text: String,
  val isDone: Boolean,
  val color: String,
  val createdAt: Long
)
