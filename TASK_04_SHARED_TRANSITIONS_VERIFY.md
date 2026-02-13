# Task 4: Verify Shared Element Transitions on API 29

**Priority:** P1 — Should Do  
**Estimated Time:** 15 minutes  
**Risk Level:** Low  
**Branch:** `android10`  
**Depends On:** Tasks 1, 2

---

## Objective

Verify that `SharedTransitionLayout` and `sharedElement()` modifier work correctly on API 29 devices. These are experimental Compose Animation APIs that should be OS-version-independent since they run in the Compose runtime (bundled in APK).

---

## Problem Analysis

**Files using shared transitions:**
- `NeonListApp.kt` — `SharedTransitionLayout` wrapping the NavHost
- `HomeScreen.kt` — `SharedTransitionScope.sharedElement()` on list cards
- `ListDetailScreen.kt` — `SharedTransitionScope.sharedElement()` on detail header

**API Used:** `@ExperimentalSharedTransitionApi`

These are all **Compose runtime APIs**, not platform APIs. They execute entirely in the app's process using Compose's rendering pipeline. In theory, they should work identically on all Android versions that support Compose (API 21+).

**Risk:** Since these are marked `@Experimental`, there's a small chance of bugs on specific configurations. The primary concern is performance, not crashes.

---

## Steps

### Step 1: Test on API 29 emulator

1. Launch app on API 29 AVD  
2. Navigate from Home → List Detail → Back
3. Observe the shared element animation (the list card should animate into the detail header)
4. Repeat for multiple lists

**Expected behavior:** Smooth shared element transition animation, identical to API 31+ behavior.

### Step 2: Check for crashes

Look at Logcat for any of these errors:
```
SharedTransitionScope
IllegalStateException
ExperimentalSharedTransitionApi
```

If crashes occur, proceed to Step 3.

### Step 3: Add failsafe wrapper (ONLY if crashes occur)

If `SharedTransitionLayout` crashes on API 29, create a version-gated wrapper:

**File:** `NeonListApp.kt`

Replace the `SharedTransitionLayout` block with a conditional:

```kotlin
import android.os.Build

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun NeonListApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val language by viewModel.currentLanguage.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkTheme = themeMode != "light"

    val strings = when (language) {
        "it" -> ItStrings
        "si" -> SiStrings
        else -> EnStrings
    }

    CompositionLocalProvider(LocalStrings provides strings) {
        NeonTheme(isDark = isDarkTheme) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+: Full shared transitions (current behavior, unchanged)
                SharedTransitionLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .clipToBounds()
                ) {
                    val sharedScope = this
                    NeonNavHost(navController, viewModel, sharedScope)
                }
            } else {
                // API 29-30: No shared transitions (safe fallback)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .clipToBounds()
                ) {
                    NeonNavHostSimple(navController, viewModel)
                }
            }
        }
    }
}
```

This would also require creating **simplified versions** of `HomeScreen` and `ListDetailScreen` composables that don't take `SharedTransitionScope` and `AnimatedContentScope` parameters. This is a significant refactor and should **only be done if crashes are confirmed**.

### Step 4: Alternative — Try-catch wrapper

A lighter approach if only specific calls crash:

```kotlin
val sharedModifier = try {
    with(sharedTransitionScope) {
        Modifier.sharedElement(
            sharedContentState = sharedState,
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
} catch (e: Exception) {
    Timber.w(e, "Shared transition not available, using fallback")
    Modifier
}
```

---

## Verification

- [ ] Navigate Home → List Detail on API 29: no crash
- [ ] Navigate Home → List Detail on API 29: animation plays (or gracefully skipped)
- [ ] Navigate back from List Detail: no crash
- [ ] Navigate Home → Search → Back: no issues
- [ ] Navigate Home → Settings → Back: no issues
- [ ] All navigation works on API 31+ (no regressions)

---

## Files Potentially Changed

| File | Action | Condition |
|------|--------|-----------|
| `NeonListApp.kt` | MODIFIED | Only if shared transitions crash on API 29 |
| `HomeScreen.kt` | MODIFIED | Only if a simplified version is needed |
| `ListDetailScreen.kt` | MODIFIED | Only if a simplified version is needed |

**Most likely outcome: NO changes needed.** Compose shared transitions should work on API 29.

---

## Technical Notes

### Why this should work

The `SharedTransitionLayout` is implemented entirely in the Compose Animation library (artifact: `androidx.compose.animation:animation`). It uses `graphicsLayer` and `Modifier.layout` internally — both of which are standard Compose APIs that work on all API levels.

The `@ExperimentalSharedTransitionApi` annotation means the API surface may change in future Compose versions, NOT that it has platform-level restrictions.

### Performance consideration

API 29 devices may be older and slower. The shared element animations involve:
- Capturing bounds of source and target elements
- Animating position, size, and clip path
- Compositing layers with alpha blending

If animations are janky (not crashing, just slow), consider:
```kotlin
val animationDuration = if (Build.VERSION.SDK_INT >= 31) 300 else 150
```

This is optional and only for polish.
