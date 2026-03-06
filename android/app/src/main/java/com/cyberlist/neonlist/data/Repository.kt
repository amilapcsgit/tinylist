package com.cyberlist.neonlist.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

import android.content.Context
import androidx.room.withTransaction
import timber.log.Timber

class Repository(
  private val context: Context,
  private val listDao: ListDao,
  private val itemDao: ItemDao,
  private val database: NeonDatabase
) {
  private val prefs = context.getSharedPreferences("neonlist_prefs", Context.MODE_PRIVATE)
  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
  }
  val lists: Flow<List<ListEntity>> = listDao.observeLists()
    .catch { e ->
      Timber.e(e, "Error observing lists")
      emit(emptyList())
    }
  val items: Flow<List<ItemEntity>> = itemDao.observeItems()
    .catch { e ->
      Timber.e(e, "Error observing items")
      emit(emptyList())
    }

  fun itemsByList(listId: String): Flow<List<ItemEntity>> = itemDao.observeItemsByList(listId)

  suspend fun seedIfEmpty() {
    val hasLists = listDao.count() > 0
    if (hasLists) {
      val hasItems = itemDao.count() > 0
      val hasSampleLists =
        listDao.getById("1") != null &&
          listDao.getById("2") != null &&
          listDao.getById("3") != null
      if (!hasItems && hasSampleLists) {
        val now = System.currentTimeMillis()
        val items = listOf(
          ItemEntity(id = "101", listId = "1", text = "Welcome to NeonList", isDone = false, color = "red", createdAt = now, order = 0),
          ItemEntity(id = "102", listId = "1", text = "Swipe right to edit", isDone = false, color = "blue", createdAt = now + 1, order = 1),
          ItemEntity(id = "103", listId = "1", text = "Swipe left to delete", isDone = true, color = "green", createdAt = now + 2, order = 2),
          ItemEntity(id = "104", listId = "2", text = "Milk 2.50", isDone = false, color = "green", createdAt = now + 3, order = 0),
          ItemEntity(id = "105", listId = "2", text = "Bread 1.20", isDone = false, color = "orange", createdAt = now + 4, order = 1)
        )
        itemDao.upsertAll(items)
        prefs.edit().putBoolean("seeded_sample_items", true).apply()
      }
      return
    }

    val now = System.currentTimeMillis()
    val lists = listOf(
      ListEntity(id = "1", title = "Todos", color = "red", createdAt = now, order = 0),
      ListEntity(id = "2", title = "Groceries", color = "green", createdAt = now, order = 1),
      ListEntity(id = "3", title = "Ideas", color = "cyan", createdAt = now, order = 2)
    )
    val items = listOf(
      ItemEntity(id = "101", listId = "1", text = "Welcome to NeonList", isDone = false, color = "red", createdAt = now, order = 0),
      ItemEntity(id = "102", listId = "1", text = "Swipe right to edit", isDone = false, color = "blue", createdAt = now + 1, order = 1),
      ItemEntity(id = "103", listId = "1", text = "Swipe left to delete", isDone = true, color = "green", createdAt = now + 2, order = 2),
      ItemEntity(id = "104", listId = "2", text = "Milk 2.50", isDone = false, color = "green", createdAt = now + 3, order = 0),
      ItemEntity(id = "105", listId = "2", text = "Bread 1.20", isDone = false, color = "orange", createdAt = now + 4, order = 1)
    )
    listDao.insertAll(lists)
    itemDao.upsertAll(items)
    prefs.edit().putBoolean("seeded_sample_items", true).apply()
  }

  suspend fun addList(title: String, color: String, order: Int) {
    val list = ListEntity(
      id = UUID.randomUUID().toString(),
      title = title,
      color = color,
      createdAt = System.currentTimeMillis(),
      order = order
    )
    listDao.insert(list)
  }

  suspend fun addListEntity(list: ListEntity) = listDao.insert(list)

  suspend fun updateList(list: ListEntity) = listDao.update(list)

  suspend fun upsertList(list: ListEntity) = listDao.upsert(list)

  suspend fun deleteList(listId: String) {
    database.withTransaction {
      listDao.deleteById(listId)
    }
  }

  suspend fun addItem(listId: String, text: String, color: String) {
    val nextOrder = (itemDao.maxOrder(listId) ?: -1L) + 1L
    val item = ItemEntity(
      id = UUID.randomUUID().toString(),
      listId = listId,
      text = text,
      isDone = false,
      color = color,
      createdAt = System.currentTimeMillis(),
      order = nextOrder
    )
    itemDao.upsert(item)
  }

  suspend fun updateItem(item: ItemEntity) = itemDao.upsert(item)

  suspend fun deleteItem(itemId: String) = itemDao.deleteById(itemId)

  suspend fun clearCompleted(listId: String) = itemDao.clearCompleted(listId)

  suspend fun reorderLists(lists: List<ListEntity>) = listDao.updateAll(lists)

  suspend fun reorderItems(items: List<ItemEntity>) = itemDao.updateAll(items)

  fun getSavedLanguage(): String? = prefs.getString("language", null)

  fun saveLanguage(code: String) {
    prefs.edit().putString("language", code).apply()
  }

  fun getSavedTheme(): String? = prefs.getString("theme", null)

  fun saveTheme(mode: String) {
    prefs.edit().putString("theme", mode).apply()
  }

  suspend fun exportJson(currentLists: List<ListEntity>, currentItems: List<ItemEntity>): String = withContext(Dispatchers.Default) {
    val payload = ExportPayload(
      lists = currentLists.map { ExportList(it.id, it.title, it.color, it.createdAt, it.order) },
      items = currentItems.map { ExportItem(it.id, it.listId, it.text, it.isDone, it.color, it.createdAt, it.order) },
      exportedAt = System.currentTimeMillis()
    )
    json.encodeToString(payload)
  }

  suspend fun importJson(content: String): ImportSummary = withContext(Dispatchers.IO) {
    val payload = try {
      json.decodeFromString<ImportPayload>(content)
    } catch (e: Exception) {
      throw IllegalArgumentException("Invalid backup JSON format.", e)
    }

    database.withTransaction {
      val existingLists = listDao.getAll()
      val nextListOrderStart = (existingLists.maxOfOrNull { it.order } ?: -1) + 1
      val resolution = ImportPlanner.resolveListTargets(
        importedLists = payload.lists,
        existingLists = existingLists,
        nextListOrderStart = nextListOrderStart,
        newId = { UUID.randomUUID().toString() }
      )

      if (resolution.listsToCreate.isNotEmpty()) {
        listDao.insertAll(resolution.listsToCreate)
      }

      val importedItemsBySourceList = payload.items.groupBy { it.listId }
      var importedItemsCount = 0

      resolution.listTargets.forEach { target ->
        val sourceItems = importedItemsBySourceList[target.sourceListId].orEmpty()
        if (sourceItems.isEmpty()) return@forEach

        val existingItems = itemDao.getByListId(target.targetListId)
        val appendedItems = ImportPlanner.sortedImportedItems(sourceItems).mapIndexed { index, item ->
          ItemEntity(
            id = UUID.randomUUID().toString(),
            listId = target.targetListId,
            text = item.text,
            isDone = item.isDone,
            color = item.color,
            createdAt = item.createdAt,
            order = (existingItems.size + index).toLong()
          )
        }
        if (appendedItems.isNotEmpty()) {
          itemDao.upsertAll(appendedItems)
          importedItemsCount += appendedItems.size
        }

        val currentItems = itemDao.getByListId(target.targetListId)
        val normalizedItems = ImportPlanner.normalizeSequentialOrder(currentItems)
        val changedItems = normalizedItems.filterIndexed { index, normalized ->
          normalized.order != currentItems[index].order
        }
        if (changedItems.isNotEmpty()) {
          itemDao.updateAll(changedItems)
        }
      }

      ImportSummary(
        listsCreated = resolution.listsCreated,
        listsMerged = resolution.listsMerged,
        itemsImported = importedItemsCount
      )
    }
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
  val createdAt: Long,
  val order: Long
)
