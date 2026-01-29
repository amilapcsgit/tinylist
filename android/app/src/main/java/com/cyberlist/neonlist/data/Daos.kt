package com.cyberlist.neonlist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
  @Query("SELECT * FROM lists ORDER BY sort_order ASC")
  fun observeLists(): Flow<List<ListEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(list: ListEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(lists: List<ListEntity>)

  @Delete
  suspend fun delete(list: ListEntity)

  @Query("DELETE FROM lists WHERE id = :listId")
  suspend fun deleteById(listId: String)

  @Query("SELECT * FROM lists WHERE id = :listId LIMIT 1")
  suspend fun getById(listId: String): ListEntity?

  @Query("SELECT COUNT(*) FROM lists")
  suspend fun count(): Int
}

@Dao
interface ItemDao {
  @Query("SELECT * FROM items ORDER BY createdAt ASC")
  fun observeItems(): Flow<List<ItemEntity>>

  @Query("SELECT * FROM items WHERE listId = :listId ORDER BY createdAt ASC")
  fun observeItemsByList(listId: String): Flow<List<ItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(item: ItemEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(items: List<ItemEntity>)

  @Delete
  suspend fun delete(item: ItemEntity)

  @Query("DELETE FROM items WHERE id = :itemId")
  suspend fun deleteById(itemId: String)

  @Query("DELETE FROM items WHERE listId = :listId")
  suspend fun deleteByListId(listId: String)

  @Query("DELETE FROM items WHERE listId = :listId AND isDone = 1")
  suspend fun clearCompleted(listId: String)

  @Query("SELECT COUNT(*) FROM items")
  suspend fun count(): Int
}
