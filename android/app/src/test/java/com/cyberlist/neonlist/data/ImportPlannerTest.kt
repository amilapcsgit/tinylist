package com.cyberlist.neonlist.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ImportPlannerTest {
  @Test
  fun resolveListTargets_mergesOnlyExactTitleMatches() {
    val existing = listOf(
      ListEntity(id = "existing-1", title = "Todo list", color = "red", createdAt = 1L, order = 0)
    )
    val imported = listOf(
      ImportList(id = "old-1", title = "Todo list", color = "cyan", createdAt = 10L, order = 0),
      ImportList(id = "old-2", title = "todo list", color = "green", createdAt = 11L, order = 1),
      ImportList(id = "old-3", title = "Groceries", color = "blue", createdAt = 12L, order = 2)
    )

    val generated = ArrayDeque(listOf("new-1", "new-2"))
    val result = ImportPlanner.resolveListTargets(
      importedLists = imported,
      existingLists = existing,
      nextListOrderStart = 1
    ) { generated.removeFirst() }

    val targetBySource = result.listTargets.associate { it.sourceListId to it.targetListId }
    assertEquals("existing-1", targetBySource["old-1"])
    assertEquals("new-1", targetBySource["old-2"])
    assertEquals("new-2", targetBySource["old-3"])
    assertEquals(2, result.listsCreated)
    assertEquals(1, result.listsMerged)
  }

  @Test
  fun normalizeSequentialOrder_reindexesMixedOrders() {
    val items = listOf(
      ItemEntity(id = "i1", listId = "l1", text = "a", isDone = false, color = "red", createdAt = 1L, order = 0L),
      ItemEntity(id = "i2", listId = "l1", text = "b", isDone = false, color = "red", createdAt = 2L, order = 189L),
      ItemEntity(id = "i3", listId = "l1", text = "c", isDone = true, color = "blue", createdAt = 3L, order = 99999L)
    )

    val normalized = ImportPlanner.normalizeSequentialOrder(items)

    assertEquals(listOf(0L, 1L, 2L), normalized.map { it.order })
  }

  @Test
  fun sortedImportedItems_supportsTimestampStyleOrderValues() {
    val imported = listOf(
      ImportItem(id = "i1", listId = "l1", text = "first", isDone = false, color = "red", createdAt = 2L, order = 1700000000000L),
      ImportItem(id = "i2", listId = "l1", text = "second", isDone = false, color = "red", createdAt = 3L, order = 1L),
      ImportItem(id = "i3", listId = "l1", text = "third", isDone = true, color = "green", createdAt = 1L, order = 1L)
    )

    val sorted = ImportPlanner.sortedImportedItems(imported)

    assertEquals(listOf("i3", "i2", "i1"), sorted.map { it.id })
  }
}
