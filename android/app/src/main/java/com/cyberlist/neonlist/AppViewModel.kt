package com.cyberlist.neonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberlist.neonlist.data.ItemEntity
import com.cyberlist.neonlist.data.ImportSummary
import com.cyberlist.neonlist.data.ListEntity
import com.cyberlist.neonlist.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortMode { MANUAL, AZ, COMPLETION }

sealed class HistoryEntry {
  data class ListDelete(val list: ListEntity, val items: List<ItemEntity>) : HistoryEntry()
  data class ItemDelete(val item: ItemEntity) : HistoryEntry()
  data class ItemComplete(val id: String, val wasDone: Boolean) : HistoryEntry()
  data class ListUpdate(val oldList: ListEntity) : HistoryEntry()
  data class ItemUpdate(val oldItem: ItemEntity) : HistoryEntry()
}

class AppViewModel(private val repository: Repository) : ViewModel() {
  private val sortMode = MutableStateFlow(SortMode.MANUAL)
  private val history = MutableStateFlow<List<HistoryEntry>>(emptyList())
  private val _language = MutableStateFlow(repository.getSavedLanguage() ?: java.util.Locale.getDefault().language)
  private val _themeMode = MutableStateFlow(repository.getSavedTheme() ?: "dark")
  val currentLanguage: StateFlow<String> = _language
  val themeMode: StateFlow<String> = _themeMode

  val lists: StateFlow<List<ListEntity>> = repository.lists
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val items: StateFlow<List<ItemEntity>> = repository.items
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val sortedLists: StateFlow<List<ListEntity>> = combine(lists, items, sortMode) { listData, itemData, mode ->
    when (mode) {
      SortMode.AZ -> listData.sortedBy { it.title.lowercase() }
      SortMode.COMPLETION -> {
        val statsByList = HashMap<String, IntArray>(listData.size)
        itemData.forEach { item ->
          val stats = statsByList.getOrPut(item.listId) { intArrayOf(0, 0) } // [done, total]
          if (item.isDone) stats[0]++
          stats[1]++
        }
        listData.sortedByDescending { list ->
          val stats = statsByList[list.id]
          val done = stats?.get(0) ?: 0
          val total = stats?.get(1) ?: 0
          if (total == 0) 0.0 else done.toDouble() / total.toDouble()
        }
      }
      SortMode.MANUAL -> listData.sortedBy { it.order }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentSortMode: StateFlow<SortMode> = sortMode

  val historyState: StateFlow<List<HistoryEntry>> = history

  fun setSortMode(mode: SortMode) {
    sortMode.value = mode
  }

  fun setLanguage(code: String) {
    _language.value = code
    viewModelScope.launch {
      repository.saveLanguage(code)
    }
  }

  fun setThemeMode(mode: String) {
    _themeMode.value = mode
    viewModelScope.launch {
      repository.saveTheme(mode)
    }
  }

  fun toggleTheme() {
    setThemeMode(if (_themeMode.value == "dark") "light" else "dark")
  }

  fun addList(title: String, color: String) {
    viewModelScope.launch {
      repository.addList(title, color, lists.value.size)
    }
  }

  fun updateList(list: ListEntity) {
    viewModelScope.launch {
      lists.value.find { it.id == list.id }?.let { old ->
        pushHistory(HistoryEntry.ListUpdate(old))
      }
      repository.updateList(list)
    }
  }

  fun deleteList(list: ListEntity, listItems: List<ItemEntity>) {
    viewModelScope.launch {
      pushHistory(HistoryEntry.ListDelete(list, listItems))
      repository.deleteList(list.id)
    }
  }

  fun reorderLists(newOrder: List<ListEntity>) {
    viewModelScope.launch {
      repository.reorderLists(newOrder)
    }
  }

  fun reorderItems(newOrder: List<ItemEntity>) {
    viewModelScope.launch {
      repository.reorderItems(newOrder)
    }
  }

  fun addItem(listId: String, text: String, color: String) {
    viewModelScope.launch {
      repository.addItem(listId, text, color)
    }
  }

  fun updateItem(item: ItemEntity) {
    viewModelScope.launch {
      items.value.find { it.id == item.id }?.let { old ->
        pushHistory(HistoryEntry.ItemUpdate(old))
      }
      repository.updateItem(item)
    }
  }

  fun deleteItem(item: ItemEntity) {
    viewModelScope.launch {
      pushHistory(HistoryEntry.ItemDelete(item))
      repository.deleteItem(item.id)
    }
  }

  fun toggleItem(item: ItemEntity) {
    viewModelScope.launch {
      pushHistory(HistoryEntry.ItemComplete(item.id, item.isDone))
      repository.updateItem(item.copy(isDone = !item.isDone))
    }
  }

  fun clearCompleted(listId: String) {
    viewModelScope.launch {
      repository.clearCompleted(listId)
    }
  }

  fun duplicateList(listId: String) {
    viewModelScope.launch {
      val source = lists.value.find { it.id == listId } ?: return@launch
      val sourceItems = items.value.filter { it.listId == listId }

      val newListId = UUID.randomUUID().toString()
      val newList = source.copy(
        id = newListId,
        title = "${source.title} Copy",
        createdAt = System.currentTimeMillis(),
        order = lists.value.size
      )

      val newItems = sourceItems.map {
        it.copy(
          id = UUID.randomUUID().toString(),
          listId = newListId,
          createdAt = System.currentTimeMillis()
        )
      }

      repository.addListEntity(newList)
      newItems.forEach { repository.updateItem(it) }
    }
  }

  fun undo() {
    val stack = history.value
    if (stack.isEmpty()) return

    val entry = stack.last()
    history.value = stack.dropLast(1)

    viewModelScope.launch {
      when (entry) {
        is HistoryEntry.ListDelete -> {
          repository.upsertList(entry.list)
          entry.items.forEach { repository.updateItem(it) }
        }
        is HistoryEntry.ItemDelete -> repository.updateItem(entry.item)
        is HistoryEntry.ItemComplete -> {
          val target = items.value.find { it.id == entry.id } ?: return@launch
          repository.updateItem(target.copy(isDone = entry.wasDone))
        }
        is HistoryEntry.ListUpdate -> repository.updateList(entry.oldList)
        is HistoryEntry.ItemUpdate -> repository.updateItem(entry.oldItem)
      }
    }
  }

  suspend fun exportJson(): String {
    return repository.exportJson(lists.value, items.value)
  }

  suspend fun importJson(content: String): ImportSummary {
    return repository.importJson(content)
  }

  private fun pushHistory(entry: HistoryEntry) {
    val trimmed = history.value.takeLast(9) + entry
    history.value = trimmed
  }
}
