package com.cyberlist.neonlist.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

interface Strings {
  val appName: String get() = "NeonList"

  // Home Screen
  val homeTitle: String
  val undo: String
  val sort: String
  val sortAZ: String
  val sortByCompletion: String
  val manualOrder: String
  val noData: String
  val tapPlusToInitialize: String
  val newList: String
  val create: String
  val cancel: String
  val editList: String
  val save: String
  val title: String
  val duplicate: String
  val addList: String

  // List Detail Screen
  val clearSelection: String
  val clearCompleted: String
  val duplicateList: String
  val emptyList: String
  val newItem: String
  val whatNeedsToBeDone: String
  val addItem: String
  val deleteItemQuestion: String
  val delete: String
  val editItem: String
  val updateItemText: String
  val selectedSum: String
  val totalSum: String
  val itemsCount: (Int) -> String

  // Settings Screen
  val settings: String
  val appearance: String
  val theme: String
  val themeLockedNote: String
  val data: String
  val exportBackup: String
  val exportBackupNote: String
  val lists: String
  val items: String
  val neonList: String
  val androidBuild: String
  val language: String
  val back: String

  // Search Screen
  val search: String
  val searchPlaceholder: String
  val matchingLists: String
  val matchingItems: String
  val noMatchesFound: String
}

object EnStrings : Strings {
  override val homeTitle = "Neon Lists"
  override val undo = "Undo"
  override val sort = "Sort"
  override val sortAZ = "Sort A-Z"
  override val sortByCompletion = "Sort by Completion"
  override val manualOrder = "Manual Order"
  override val noData = "NO DATA"
  override val tapPlusToInitialize = "Tap + to initialize new list"
  override val newList = "New List"
  override val create = "CREATE"
  override val cancel = "CANCEL"
  override val editList = "Edit List"
  override val save = "SAVE"
  override val title = "Title"
  override val duplicate = "DUPLICATE"
  override val addList = "ADD LIST"

  override val clearSelection = "Clear Selection"
  override val clearCompleted = "Clear Completed"
  override val duplicateList = "Duplicate List"
  override val emptyList = "EMPTY LIST"
  override val newItem = "New Item"
  override val whatNeedsToBeDone = "What needs to be done?"
  override val addItem = "ADD ITEM"
  override val deleteItemQuestion = "Delete Item?"
  override val delete = "DELETE"
  override val editItem = "Edit Item"
  override val updateItemText = "Update item text"
  override val selectedSum = "SELECTED SUM"
  override val totalSum = "TOTAL SUM"
  override val itemsCount = { count: Int -> if (count == 1) "1 item" else "$count items" }

  override val settings = "Settings"
  override val appearance = "APPEARANCE"
  override val theme = "Theme"
  override val themeLockedNote = "Currently locked to Cyberpunk Dark Mode."
  override val data = "DATA"
  override val exportBackup = "Export Backup"
  override val exportBackupNote = "Save your lists as JSON file"
  override val lists = "LISTS"
  override val items = "ITEMS"
  override val neonList = "NEON LIST"
  override val androidBuild = "ANDROID BUILD"
  override val language = "Language"
  override val back = "Back"

  override val search = "Search"
  override val searchPlaceholder = "Search lists and items..."
  override val matchingLists = "MATCHING LISTS"
  override val matchingItems = "MATCHING ITEMS"
  override val noMatchesFound = "NO MATCHES FOUND"
}

object ItStrings : Strings {
  override val homeTitle = "Liste Neon"
  override val undo = "Annulla"
  override val sort = "Ordina"
  override val sortAZ = "Ordina A-Z"
  override val sortByCompletion = "Ordina per Completamento"
  override val manualOrder = "Ordine Manuale"
  override val noData = "NESSUN DATO"
  override val tapPlusToInitialize = "Tocca + per creare una nuova lista"
  override val newList = "Nuova Lista"
  override val create = "CREA"
  override val cancel = "ANNULLA"
  override val editList = "Modifica Lista"
  override val save = "SALVA"
  override val title = "Titolo"
  override val duplicate = "DUPLICA"
  override val addList = "AGGIUNGI LISTA"

  override val clearSelection = "Cancella Selezione"
  override val clearCompleted = "Cancella Completati"
  override val duplicateList = "Duplica Lista"
  override val emptyList = "LISTA VUOTA"
  override val newItem = "Nuovo Elemento"
  override val whatNeedsToBeDone = "Cosa c'è da fare?"
  override val addItem = "AGGIUNGI ELEMENTO"
  override val deleteItemQuestion = "Elimina Elemento?"
  override val delete = "ELIMINA"
  override val editItem = "Modifica Elemento"
  override val updateItemText = "Aggiorna testo elemento"
  override val selectedSum = "SOMMA SELEZIONATA"
  override val totalSum = "SOMMA TOTALE"
  override val itemsCount = { count: Int -> if (count == 1) "1 elemento" else "$count elementi" }

  override val settings = "Impostazioni"
  override val appearance = "ASPETTO"
  override val theme = "Tema"
  override val themeLockedNote = "Attualmente bloccato in modalità Dark Cyberpunk."
  override val data = "DATI"
  override val exportBackup = "Esporta Backup"
  override val exportBackupNote = "Salva le tue liste come file JSON"
  override val lists = "LISTE"
  override val items = "ELEMENTI"
  override val neonList = "LISTA NEON"
  override val androidBuild = "BUILD ANDROID"
  override val language = "Lingua"
  override val back = "Indietro"

  override val search = "Cerca"
  override val searchPlaceholder = "Cerca liste ed elementi..."
  override val matchingLists = "LISTE CORRISPONDENTI"
  override val matchingItems = "ELEMENTI CORRISPONDENTI"
  override val noMatchesFound = "NESSUNA CORRISPONDENZA TROVATA"
}

object SiStrings : Strings {
  override val homeTitle = "නියොන් ලැයිස්තු"
  override val undo = "පෙර තත්වයට"
  override val sort = "පෙළගස්වන්න"
  override val sortAZ = "අකාරාදී පිළිවෙලට"
  override val sortByCompletion = "අවසන් කළ ප්‍රමාණය අනුව"
  override val manualOrder = "අභිරුචි පිළිවෙල"
  override val noData = "දත්ත නොමැත"
  override val tapPlusToInitialize = "නව ලැයිස්තුවක් සෑදීමට + තට්ටු කරන්න"
  override val newList = "නව ලැයිස්තුව"
  override val create = "සාදන්න"
  override val cancel = "අවලංගු කරන්න"
  override val editList = "ලැයිස්තුව සංස්කරණය"
  override val save = "සුරකින්න"
  override val title = "මාතෘකාව"
  override val duplicate = "අනුපිටපත් කරන්න"
  override val addList = "ලැයිස්තුවක් එක් කරන්න"

  override val clearSelection = "තේරීම ඉවත් කරන්න"
  override val clearCompleted = "අවසන් කළ දෑ ඉවත් කරන්න"
  override val duplicateList = "ලැයිස්තුව අනුපිටපත් කරන්න"
  override val emptyList = "හිස් ලැයිස්තුව"
  override val newItem = "නව අයිතමය"
  override val whatNeedsToBeDone = "කුමක් කළ යුතුද?"
  override val addItem = "අයිතමය එක් කරන්න"
  override val deleteItemQuestion = "අයිතමය මකන්නද?"
  override val delete = "මකන්න"
  override val editItem = "අයිතමය සංස්කරණය"
  override val updateItemText = "අයිතමය යාවත්කාලීන කරන්න"
  override val selectedSum = "තෝරාගත් එකතුව"
  override val totalSum = "මුළු එකතුව"
  override val itemsCount = { count: Int -> "අයිතම $count" }

  override val settings = "සැකසුම්"
  override val appearance = "පෙනුම"
  override val theme = "තේමාව"
  override val themeLockedNote = "දැනට සයිබර්පන්ක් අඳුරු මාදිලියට සීමා වී ඇත."
  override val data = "දත්ත"
  override val exportBackup = "දත්ත පිටපතක් ලබාගන්න"
  override val exportBackupNote = "ඔබේ ලැයිස්තු JSON ගොනුවක් ලෙස සුරකින්න"
  override val lists = "ලැයිස්තු"
  override val items = "අයිතම"
  override val neonList = "නියොන් ලැයිස්තුව"
  override val androidBuild = "ඇන්ඩ්‍රොයිඩ් සංස්කරණය"
  override val language = "භාෂාව"
  override val back = "ආපසු"

  override val search = "සොයන්න"
  override val searchPlaceholder = "ලැයිස්තු සහ අයිතම සොයන්න..."
  override val matchingLists = "ගැලපෙන ලැයිස්තු"
  override val matchingItems = "ගැලපෙන අයිතම"
  override val noMatchesFound = "කිසිවක් හමු නොවීය"
}

val LocalStrings = staticCompositionLocalOf<Strings> { EnStrings }
