# Task 3: Window Insets Failsafe for API 29

**Priority:** P1 — Should Do  
**Estimated Time:** 20 minutes  
**Risk Level:** Medium  
**Branch:** `android10`  
**Depends On:** Tasks 1, 2

---

## Objective

Verify and fix `Modifier.systemBarsPadding()` behavior on API 29 devices. The edge-to-edge display model changed significantly in API 30 and 35. API 29 uses the legacy window insets model.

---

## Problem Analysis

**File:** `android/app/src/main/java/com/cyberlist/neonlist/ui/NeonListApp.kt`  
**Line 45:**
```kotlin
.systemBarsPadding()
```

On API 30+ (and especially API 35+), `enableEdgeToEdge()` or `WindowCompat.setDecorFitsSystemWindows(window, false)` is used to let the app draw behind system bars, and `systemBarsPadding()` accounts for the insets.

On API 29, the legacy model applies:
- `setDecorFitsSystemWindows` doesn't exist (API 30+)
- System bars insets may be reported differently
- The app may have double padding or incorrect padding

---

## Steps

### Step 1: Check if `enableEdgeToEdge()` is called

**File:** `android/app/src/main/java/com/cyberlist/neonlist/MainActivity.kt`

Look at the current `MainActivity.kt`:
```kotlin
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val app = application as NeonListApplication
    setContent {
      val vm: AppViewModel = viewModel(factory = AppViewModelFactory(app.repository))
      NeonListApp(vm)
    }
  }
}
```

**Current status:** There is NO explicit call to `enableEdgeToEdge()`. The app relies on `WindowInsets = WindowInsets(0, 0, 0, 0)` in the Scaffold (see `NeonScaffold.kt` line 73). This means the Scaffold doesn't add any content window insets of its own.

### Step 2: Verify behavior on API 29 emulator

1. Create an API 29 AVD in Android Studio (Pixel 4, API 29, x86_64)
2. Install the debug APK
3. Check:
   - Does the app content render behind the status bar? 
   - Does `systemBarsPadding()` correctly add padding for the status bar and navigation bar?
   - Is there any overlapping content or double padding?

### Step 3: Apply fix if needed

**If padding is incorrect**, modify `NeonListApp.kt` to use version-gated insets:

```kotlin
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView

// In NeonListApp composable:
val insetModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    Modifier.systemBarsPadding()
} else {
    // API 29 fallback: use standard padding
    Modifier.systemBarsPadding() // try this first, only change if broken
}

SharedTransitionLayout(
    modifier = Modifier
        .fillMaxSize()
        .then(insetModifier)
        .clipToBounds()
) {
```

**If `systemBarsPadding()` works correctly on API 29** (which it likely does because the Compose BOM handles backward compatibility internally), then **no code change is needed**. Just document this verification.

### Step 4: Alternative — Explicit edge-to-edge setup

If the insets need help, add explicit edge-to-edge configuration to `MainActivity.kt`:

```kotlin
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge() // Add this line
    super.onCreate(savedInstanceState)
    // ... rest unchanged
  }
}
```

The `enableEdgeToEdge()` function from `activity-compose:1.9.2` is designed to work on API 21+ and handles version differences internally. This is the **recommended approach** for cross-version compatibility.

### Step 5: Test edge cases

On the API 29 emulator, verify:
- [ ] Status bar area: no content rendered behind it (or properly padded)
- [ ] Navigation bar area: no FAB or content hidden behind it
- [ ] Landscape orientation: insets still correct
- [ ] Keyboard open: content not obscured by soft keyboard

---

## Verification

- [ ] App displays correctly on API 29 emulator
- [ ] No content overlap with system bars
- [ ] FAB is fully visible and tappable
- [ ] Header bar is below the status bar
- [ ] No regressions on API 31+ devices

---

## Files Potentially Changed

| File | Action | Condition |
|------|--------|-----------|
| `MainActivity.kt` | MODIFIED | If `enableEdgeToEdge()` needs to be added |
| `NeonListApp.kt` | MODIFIED | Only if `systemBarsPadding()` fails on API 29 |

**No changes needed if `systemBarsPadding()` works correctly on API 29** (likely case with modern Compose BOM).

---

## Technical Notes

### Why this is likely a non-issue

Jetpack Compose's `systemBarsPadding()` internally uses `WindowInsetsCompat`, which is part of AndroidX and handles API differences. The `activity-compose:1.9.2` library includes `enableEdgeToEdge()` support down to API 21.

The main risk is if the activity's window flags conflict with Compose's inset handling. Since the app uses `Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0))` and handles insets via `systemBarsPadding()` at the root layout, this should work correctly.

### Fallback: Manual status bar height

If all else fails, you can compute and apply manual padding:

```kotlin
val statusBarHeight = with(LocalDensity.current) {
    WindowInsets.statusBars.getTop(this).toDp()
}
val navBarHeight = with(LocalDensity.current) {
    WindowInsets.navigationBars.getBottom(this).toDp()
}
```
