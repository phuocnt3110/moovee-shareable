# CLAUDE.md - Data Layer Context

## Responsibility
Data layer chứa models, repositories, và local storage. Cung cấp data cho UI layer.

## Structure
```
data/
├── model/       ← Data classes (domain objects)
├── repository/  ← Data access abstraction
└── local/       ← Local storage (SharedPreferences, Room, etc.)
```

## Patterns

### Model
```kotlin
data class XxxItem(
    val id: String,
    val title: String,
    val description: String
)
```

### Repository
```kotlin
class XxxRepository(private val context: Context) {
    fun getItems(): List<XxxItem> { ... }
    fun getItemById(id: String): XxxItem? { ... }
    fun saveItem(item: XxxItem) { ... }
}
```

### Local Storage
- SharedPreferences cho simple key-value
- Room Database cho structured data (nếu cần)

## Current Status
- `model/` — Empty (chờ implement per app)
- `repository/` — Empty (chờ implement per app)
- `local/` — Empty (chờ implement per app)

## Dependencies
- Gson (`com.google.code.gson:gson:2.13.1`) — JSON serialization
- Coroutines (`kotlinx-coroutines-android:1.9.0`) — Async operations
