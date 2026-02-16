# 🤖 Agent Prompt: Android 10 Compatibility Implementation

## Mission Statement

Implement backward compatibility for Android 10 (API 29) in the NeonList Android application by following the detailed task instructions provided in the repository. The current app targets `minSdk = 31` (Android 12); your job is to lower this to `minSdk = 29` while ensuring zero regressions on modern devices (API 31+).

---

## Prerequisites & Setup

### Step 1: Pull the android10 branch
```bash
cd c:\Users\Amilapcs\source\repos\Tinylist-antigravity\tinylist
git fetch origin
git checkout android10
git status
```

Verify you are on the `android10` branch and that the working directory is clean.

### Step 2: Read the master plan
Read the complete analysis and strategy in:
- **`ANDROID10_SUPPORT_PLAN.md`** — Master plan with incompatibility analysis and architecture overview

### Step 3: Understand the task structure
There are **6 sequential tasks**, each with its own markdown file:
1. `TASK_01_GRADLE_MINSDK.md` — Change minSdk to 29
2. `TASK_02_SPLASH_SCREEN_FAILSAFE.md` — Fix splash screen compatibility ⚠️ CRITICAL
3. `TASK_03_WINDOW_INSETS_FAILSAFE.md` — Verify/fix window insets
4. `TASK_04_SHARED_TRANSITIONS_VERIFY.md` — Verify shared element animations
5. `TASK_05_EXPORT_VERIFY.md` — Verify JSON export
6. `TASK_06_EMULATOR_TESTING.md` — Full integration testing

**You must execute these tasks IN ORDER.** Each task has dependencies on the previous ones.

---

## Execution Instructions

For each task file:

### Read the task file completely
Each task file contains:
- ✅ **Objective** — what needs to be accomplished
- ✅ **Problem Analysis** — why this task is needed
- ✅ **Steps** — detailed step-by-step instructions with exact file paths, line numbers, and code snippets
- ✅ **Verification** — checklist of what to confirm
- ✅ **Files Changed** — summary of modifications
- ✅ **Technical Notes** — additional context

### Execute the task
Follow the steps EXACTLY as written. Every code snippet, file path, and command is provided.

### Verify completion
Complete the verification checklist at the end of each task before moving to the next one.

### Document issues
If you encounter ANY unexpected behavior, crashes, or test failures:
1. Document the issue clearly
2. Check the "Technical Notes" section for troubleshooting
3. Implement the failsafe/fallback approach if provided
4. Continue to the next task

---

## Detailed Task Execution Order

### ⚙️ TASK 1: Change minSdk to 29 (P0 — Must Do)
**File:** `TASK_01_GRADLE_MINSDK.md`  
**Time:** 5 minutes  
**Action:**
- Edit `android/app/build.gradle.kts` line 15
- Change `minSdk = 31` to `minSdk = 29`
- Run Gradle sync and `assembleDebug` to verify compilation

**Verification:**
- [ ] Build succeeds
- [ ] No unexpected compilation errors
- [ ] Lint warnings about splash screen are expected (fixed in Task 2)

---

### 🎨 TASK 2: Splash Screen Failsafe (P0 — Must Do) ⚠️ CRITICAL
**File:** `TASK_02_SPLASH_SCREEN_FAILSAFE.md`  
**Time:** 15 minutes  
**Action:**
- Create NEW file: `android/app/src/main/res/values-v31/themes.xml` with full theme including splash attributes
- Modify EXISTING file: `android/app/src/main/res/values/themes.xml` to remove all `windowSplashScreen*` attributes
- Ensure both files define `Theme.NeonList` style

**Why this is critical:** The splash screen attributes (`android:windowSplashScreenBackground`, etc.) were added in API 31. Using them on API 29 will cause runtime crashes.

**Verification:**
- [ ] `values-v31/themes.xml` exists and contains splash attributes
- [ ] `values/themes.xml` does NOT contain any `windowSplashScreen*` attributes
- [ ] Build succeeds with no splash-related lint errors

---

### 🪟 TASK 3: Window Insets Verification (P1 — Should Do)
**File:** `TASK_03_WINDOW_INSETS_FAILSAFE.md`  
**Time:** 20 minutes  
**Action:**
- This is primarily a VERIFICATION task
- Test `systemBarsPadding()` behavior on API 29 emulator (you'll need to create this in Task 6)
- If padding is incorrect, add `enableEdgeToEdge()` call to `MainActivity.onCreate()`
- Most likely: NO code changes needed (Compose handles this internally)

**Verification:**
- [ ] Content doesn't overlap with status bar
- [ ] FAB isn't hidden behind navigation bar
- [ ] No double padding

---

### 🎬 TASK 4: Shared Transitions Verification (P1 — Should Do)
**File:** `TASK_04_SHARED_TRANSITIONS_VERIFY.md`  
**Time:** 15 minutes  
**Action:**
- This is primarily a VERIFICATION task
- Test navigation Home → List Detail on API 29 emulator
- Observe shared element animations
- Most likely: NO code changes needed (Compose animations work across API levels)

**Verification:**
- [ ] Navigation works without crashes
- [ ] Animations play (or gracefully degrade)

---

### 💾 TASK 5: Export Verification (P2 — Nice to Have)
**File:** `TASK_05_EXPORT_VERIFY.md`  
**Time:** 10 minutes  
**Action:**
- This is purely a VERIFICATION task
- Test JSON export feature on API 29 emulator
- Expected: NO code changes needed (SAF works on API 19+)

**Verification:**
- [ ] Export button works
- [ ] File picker opens
- [ ] JSON file is saved correctly

---

### 🧪 TASK 6: Full Integration Testing (P0 — Must Do)
**File:** `TASK_06_EMULATOR_TESTING.md`  
**Time:** 30 minutes  
**Action:**
- Create API 29 AVD (Android Virtual Device) in Android Studio
- Install the modified APK: `cd android && ./gradlew installDebug`
- Execute the comprehensive test matrix (60+ test cases across 7 categories)
- Also test on API 35 emulator to verify NO REGRESSIONS

**Test Categories:**
1. App Launch & Theme (7 tests)
2. Home Screen & Lists (17 tests)
3. List Detail Screen (16 tests)
4. Search Screen (6 tests)
5. Settings Screen (11 tests)
6. Animations & Performance (7 tests)
7. Rotation & Configuration (4 tests)

**Verification:**
- [ ] All P0 tests pass on API 29
- [ ] All P0 tests pass on API 35 (regression check)
- [ ] No crashes during 10+ minutes of usage

---

## Expected Outcomes

### Code Changes Summary
After completing all tasks, the following files should be modified:

| File | Change Type | Description |
|------|-------------|-------------|
| `android/app/build.gradle.kts` | MODIFIED | `minSdk = 31` → `minSdk = 29` |
| `android/app/src/main/res/values/themes.xml` | MODIFIED | Removed API 31+ splash attributes |
| `android/app/src/main/res/values-v31/themes.xml` | **CREATED** | Full theme with splash attributes |
| `android/app/src/main/java/com/cyberlist/neonlist/MainActivity.kt` | *MAYBE* | Add `enableEdgeToEdge()` if Task 3 requires it |

**All other files should remain unchanged** unless specific issues are discovered during testing.

---

## Success Criteria

Before marking this work complete, ensure:
- [x] `minSdk = 29` in build.gradle.kts
- [x] App compiles without errors
- [x] Splash screen doesn't crash on API 29
- [x] App installs and runs on API 29 emulator
- [x] All core features work on API 29 (create/edit/delete lists, swipe actions, search, settings, export)
- [x] No regressions on API 31+ emulator (test the same features)
- [x] All 6 task verification checklists are complete

---

## Commit Strategy

After completing all tasks successfully:

```bash
git add android/app/build.gradle.kts
git add android/app/src/main/res/values/themes.xml
git add android/app/src/main/res/values-v31/themes.xml
# Add MainActivity.kt only if you modified it in Task 3
git commit -m "feat: Add Android 10 (API 29) backward compatibility

- Lower minSdk from 31 to 29
- Split themes.xml into base + values-v31 for splash screen attributes
- Verified all features work on API 29 devices
- No regressions on API 31+ devices

Closes #[issue-number] (if applicable)"

git push origin android10
```

---

## Troubleshooting

### If Task 2 build fails after theme split
- Verify both `values/themes.xml` and `values-v31/themes.xml` define the exact same style name: `Theme.NeonList`
- Ensure `values-v31/` directory exists in the correct location
- Clean and rebuild: `cd android && ./gradlew clean assembleDebug`

### If Task 3 shows padding issues
- Add `enableEdgeToEdge()` to MainActivity as instructed in Task 3
- This is a 1-line change and is well-documented in the task file

### If Task 4 animations crash
- Implement the version-gated wrapper provided in the task file
- This is unlikely but the fallback code is ready to use

### If emulator won't start in Task 6
- Ensure you have the API 29 system image downloaded in Android Studio
- Try creating a simpler device (Pixel 4, 2GB RAM)
- Check that hardware acceleration is enabled

---

## Notes for the Agent

1. **Follow the order strictly** — each task builds on the previous one
2. **Read ALL instructions** in each task file before making changes
3. **Use the exact file paths** provided — they are all verified
4. **Copy code snippets exactly** — they are tested and ready to use
5. **Don't skip verification** — each checklist is there to catch issues early
6. **Document everything** — if something unexpected happens, note it clearly
7. **Test on both API 29 and API 35** — backward compatibility + no regressions

---

## Final Deliverables

When you're done, provide:
1. ✅ Summary of files modified
2. ✅ Confirmation that all 6 tasks are complete
3. ✅ Results of API 29 testing (pass/fail with details)
4. ✅ Results of API 35 regression testing (pass/fail)
5. ✅ Git commit SHA of the completed work
6. ✅ Any issues encountered and how they were resolved

---

## Ready to Begin?

Start with:
```bash
cd c:\Users\Amilapcs\source\repos\Tinylist-antigravity\tinylist
git checkout android10
cat TASK_01_GRADLE_MINSDK.md
```

Good luck! 🚀
