# NeonList Technical Architecture

This document provides a deep dive into the technical implementation of NeonList.

## Technology Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room Persistence Library
- **State Management**: Kotlin Flows & StateFlow (MVVM)
- **Dependency Injection**: Manual injection via `ViewModelFactory`
- **Concurrency**: Kotlin Coroutines
- **JSON Serialization**: Kotlinx Serialization
- **Components**: 
    - [Reorderable](https://github.com/Calvin-Sh/Reorderable) for drag-and-drop lists.
    - Custom multi-axis swipe action system.

## Data Layer

### Room Database
The database consists of two primary entities:
- `ListEntity`: Represents a collection of items.
- `ItemEntity`: Represents a single task or entry within a list.

### Repository Pattern
The `Repository` acting as a single source of truth, managing:
- Database operations via DAOs.
- Preference management (e.g., language settings).
- Data export logic.

## State Management (`AppViewModel`)

The `AppViewModel` manages the entire application state. It uses `StateFlow` to expose reactive data streams to the UI.

Key features implemented in the ViewModel:
- **Sorting Logic**: Supports Manual, Alphabetical, and Completion-based sorting.
- **Undo System**: Uses a `HistoryEntry` sealed class to track actions (deletions, updates, completions) in a memory-bounded stack.

## UI Components

### Navigation
The app uses a simple state-based navigation (handled in `MainActivity`) to switch between:
- `HomeScreen`: Overview of all lists.
- `ListDetailScreen`: Content of a specific list.
- `SearchScreen`: Global search functionality.
- `SettingsScreen`: App configuration and data management.

### Custom Components
- **NeonScaffold**: A customized scaffold that provides the signature cyberpunk look and common navigation actions.
- **Multi-Axis Swipe**: Located in `ui/components/SwipeActions.kt`, this allows for unique 4-way gestures on cards.

## Feature Spotlights

### Numeric Sum Extraction
NeonList features an "extracted sum" mode in the `ListDetailScreen`. It uses a regular expression to find numeric values at the end of item text:
```kotlin
private val numericRegex = Regex("(-?(?:\\d+[.,])?\\d+)(?=\\D*$)")
```
This allows users to track costs or quantities simply by typing them in the item text.

### Multilingual Support
Localization is handled in `ui/Localization.kt`, using a `CompositionLocal` provider (`LocalStrings`) to inject strings into the UI hierarchy based on the user's selected language.
