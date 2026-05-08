package com.cyberlist.neonlist.data

import kotlinx.serialization.Serializable

@Serializable
data class ImportPayload(
  val lists: List<ImportList> = emptyList(),
  val items: List<ImportItem> = emptyList(),
  val exportedAt: Long? = null
)

@Serializable
data class ImportList(
  val id: String,
  val title: String,
  val color: String,
  val createdAt: Long,
  val order: Int
)

@Serializable
data class ImportItem(
  val id: String,
  val listId: String,
  val text: String,
  val isDone: Boolean,
  val color: String,
  val createdAt: Long,
  val order: Long
)

data class ImportSummary(
  val listsCreated: Int,
  val listsMerged: Int,
  val itemsImported: Int
)

internal data class ListTarget(
  val sourceListId: String,
  val targetListId: String
)

internal data class ListTargetResolution(
  val listTargets: List<ListTarget>,
  val listsToCreate: List<ListEntity>,
  val listsCreated: Int,
  val listsMerged: Int
)

internal object ImportPlanner {
  fun resolveListTargets(
    importedLists: List<ImportList>,
    existingLists: List<ListEntity>,
    nextListOrderStart: Int,
    newId: () -> String
  ): ListTargetResolution {
    var nextOrder = nextListOrderStart
    var listsCreated = 0
    var listsMerged = 0
    val listsToCreate = mutableListOf<ListEntity>()
    val targets = mutableListOf<ListTarget>()
    val byTitle = existingLists.associateBy { it.title }.toMutableMap()

    importedLists
      .sortedWith(compareBy<ImportList> { it.order }.thenBy { it.createdAt })
      .forEach { imported ->
        val matched = byTitle[imported.title]
        if (matched != null) {
          listsMerged += 1
          targets += ListTarget(sourceListId = imported.id, targetListId = matched.id)
        } else {
          val created = ListEntity(
            id = newId(),
            title = imported.title,
            color = imported.color,
            createdAt = imported.createdAt,
            order = nextOrder++
          )
          listsToCreate += created
          byTitle[created.title] = created
          listsCreated += 1
          targets += ListTarget(sourceListId = imported.id, targetListId = created.id)
        }
      }

    return ListTargetResolution(
      listTargets = targets,
      listsToCreate = listsToCreate,
      listsCreated = listsCreated,
      listsMerged = listsMerged
    )
  }

  fun sortedImportedItems(items: List<ImportItem>): List<ImportItem> {
    return items.sortedWith(compareBy<ImportItem> { it.order }.thenBy { it.createdAt })
  }

  fun normalizeSequentialOrder(items: List<ItemEntity>): List<ItemEntity> {
    return items.mapIndexed { index, item -> item.copy(order = index.toLong()) }
  }
}
