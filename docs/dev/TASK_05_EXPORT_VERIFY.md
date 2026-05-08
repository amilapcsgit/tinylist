# Task 5: Verify JSON Export on API 29

**Priority:** P2 — Nice to Have (verify correctness)  
**Estimated Time:** 10 minutes  
**Risk Level:** Low  
**Branch:** `android10`  
**Depends On:** Tasks 1, 2

---

## Objective

Verify that the JSON export feature (using `ActivityResultContracts.CreateDocument`) works correctly on API 29 devices.

---

## Problem Analysis

**File:** `android/app/src/main/java/com/cyberlist/neonlist/ui/screens/SettingsScreen.kt`

**Lines 78-86:**
```kotlin
val exportLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
) { uri: Uri? ->
    if (uri == null) return@rememberLauncherForActivityResult
    scope.launch {
        val json = viewModel.exportJson()
        writeToUri(context, uri, json)
    }
}
```

**Analysis:**
- `ActivityResultContracts.CreateDocument(mimeType)` — Introduced in `activity:1.2.0`, with the MIME type constructor available since `activity:1.6.0`. The app uses `activity-compose:1.9.2` which includes the full API. This is a **library API**, not platform API, so it works on API 19+.
- `ContentResolver.openOutputStream()` — Available since API 1.
- `Intent.ACTION_CREATE_DOCUMENT` — Part of Storage Access Framework, available since API 19.

**Conclusion:** This should work on API 29 without any changes.

---

## Steps

### Step 1: Test export on API 29 emulator

1. Launch app on API 29 AVD
2. Create at least one list with some items
3. Go to Settings screen
4. Tap "Export Backup"
5. Verify:
   - File picker/SAF dialog opens
   - Can choose a location to save
   - File is saved with correct filename (`neonlist-backup-YYYY-MM-DD.json`)
   - File contains valid JSON data

### Step 2: Verify exported JSON content

Open the exported file and check:
```json
{
  "lists": [...],
  "items": [...],
  "exportedAt": 1234567890
}
```

### Step 3: Check for scoped storage differences

API 29 introduced **Scoped Storage** but with a transitional behavior:
- `android:requestLegacyExternalStorage` is available but NOT needed here
- The app uses `ACTION_CREATE_DOCUMENT` (SAF), which is explicitly designed to work WITH scoped storage
- No direct file system access is used

**No additional manifest flags are needed.**

---

## Verification

- [ ] "Export Backup" button is visible on API 29
- [ ] Tapping it opens the system file picker
- [ ] Can save to Downloads or other location
- [ ] Saved file contains valid JSON
- [ ] No crashes during the export process
- [ ] Export still works on API 31+ (no regression)

---

## Files Changed

**None.** This task is purely verification. No code changes are expected.

---

## Edge Cases to Check

1. **No storage permission:** SAF (Storage Access Framework) does NOT require storage permissions. The system grants URI-based access to the chosen file. This should work without `READ_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE` permissions.

2. **Large data:** If the user has many lists/items, verify the export doesn't OOM. The current implementation uses `kotlinx.serialization.json.Json.encodeToString()` which is efficient.

3. **Cancel export:** If the user cancels the file picker, `uri` will be null. The code handles this with the early return on line 81.
