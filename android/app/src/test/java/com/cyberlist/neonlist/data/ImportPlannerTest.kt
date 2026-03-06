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
}
