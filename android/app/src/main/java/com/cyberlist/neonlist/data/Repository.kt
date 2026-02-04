package com.cyberlist.neonlist.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

import android.content.Context

class Repository(
  private val context: Context,
  private val listDao: ListDao,
  private val itemDao: ItemDao
) {
  private val prefs = context.getSharedPreferences("neonlist_prefs", Context.MODE_PRIVATE)
  private val json = Json { prettyPrint = true }
  val lists: Flow<List<ListEntity>> = listDao.observeLists()
  val items: Flow<List<ItemEntity>> = itemDao.observeItems()

  fun itemsByList(listId: String): Flow<List<ItemEntity>> = itemDao.observeItemsByList(listId)

  suspend fun seedIfEmpty() {
    if (listDao.count() > 0) return

    val now = System.currentTimeMillis()
    val lists = listOf(
      ListEntity(id = "1", title = "Todos", color = "red", createdAt = now, order = 0),
      ListEntity(id = "2", title = "Groceries", color = "green", createdAt = now, order = 1),
      ListEntity(id = "3", title = "Ideas", color = "cyan", createdAt = now, order = 2)
    )
    val items = listOf(
      ItemEntity(id = "101", listId = "1", text = "Welcome to NeonList", isDone = false, color = "red", createdAt = now),
      ItemEntity(id = "102", listId = "1", text = "Swipe right to edit", isDone = false, color = "blue", createdAt = now + 1),
      ItemEntity(id = "103", listId = "1", text = "Swipe left to delete", isDone = true, color = "green", createdAt = now + 2),
      ItemEntity(id = "104", listId = "2", text = "Milk 2.50", isDone = false, color = "green", createdAt = now + 3),
      ItemEntity(id = "105", listId = "2", text = "Bread 1.20", isDone = false, color = "orange", createdAt = now + 4)
    )
    listDao.upsertAll(lists)
    itemDao.upsertAll(items)
  }

  suspend fun addList(title: String, color: String, order: Int) {
    val list = ListEntity(
      id = UUID.randomUUID().toString(),
      title = title,
      color = color,
      createdAt = System.currentTimeMillis(),
      order = order
    )
    listDao.upsert(list)
  }

  suspend fun updateList(list: ListEntity) = listDao.upsert(list)

  suspend fun deleteList(listId: String) {
    listDao.deleteById(listId)
    itemDao.deleteByListId(listId)
  }

  suspend fun addItem(listId: String, text: String, color: String) {
    val item = ItemEntity(
      id = UUID.randomUUID().toString(),
      listId = listId,
      text = text,
      isDone = false,
      color = color,
      createdAt = System.currentTimeMillis()
    )
    itemDao.upsert(item)
  }

  suspend fun updateItem(item: ItemEntity) = itemDao.upsert(item)

  suspend fun deleteItem(itemId: String) = itemDao.deleteById(itemId)

  suspend fun clearCompleted(listId: String) = itemDao.clearCompleted(listId)

  suspend fun reorderLists(lists: List<ListEntity>) = listDao.upsertAll(lists)

  fun getSavedLanguage(): String? = prefs.getString("language", null)

  fun saveLanguage(code: String) {
    prefs.edit().putString("language", code).apply()
  }

  suspend fun exportJson(currentLists: List<ListEntity>, currentItems: List<ItemEntity>): String = withContext(Dispatchers.Default) {
    val payload = ExportPayload(
      lists = currentLists.map { ExportList(it.id, it.title, it.color, it.createdAt, it.order) },
      items = currentItems.map { ExportItem(it.id, it.listId, it.text, it.isDone, it.color, it.createdAt) },
      exportedAt = System.currentTimeMillis()
    )
    json.encodeToString(payload)
  }
}

@Serializable
private data class ExportPayload(
  val lists: List<ExportList>,
  val items: List<ExportItem>,
  val exportedAt: Long
)

@Serializable
private data class ExportList(
  val id: String,
  val title: String,
  val color: String,
  val createdAt: Long,
  val order: Int
)

@Serializable
private data class ExportItem(
  val id: String,
  val listId: String,
  val text: String,
  val isDone: Boolean,
  val color: String,
  val createdAt: Long
)
