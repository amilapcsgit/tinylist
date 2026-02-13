# Task 6: Full Integration Testing on API 29 Emulator

**Priority:** P0 — Must Do  
**Estimated Time:** 30 minutes  
**Risk Level:** N/A (testing only)  
**Branch:** `android10`  
**Depends On:** Tasks 1-5 completed

---

## Objective

Perform comprehensive end-to-end testing of the NeonList app on an Android 10 (API 29) emulator to verify all features work correctly after the backward compatibility changes.

---

## Prerequisites

- All Tasks 1-5 completed and committed
- Android Studio with AVD Manager
- API 29 system image downloaded

---

## Setup

### Step 1: Create API 29 AVD

1. Open Android Studio → Tools → Device Manager
2. Create Virtual Device:
   - **Device:** Pixel 4 (or similar)
   - **System Image:** API 29 (Android 10.0, x86_64, Google APIs)
   - **Name:** `Pixel4_API29_NeonList`
   - **RAM:** 2048 MB
   - **Storage:** 2048 MB
3. Start the emulator

### Step 2: Build and install

```bash
cd android
./gradlew installDebug
```

OR use Android Studio Run button targeting the API 29 emulator.

---

## Test Matrix

### A. App Launch & Theme
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| A1 | Cold launch app | App opens without crash, dark theme visible | ☐ |
| A2 | Verify no splash screen crash | App starts (may skip splash on API 29) | ☐ |
| A3 | Check status bar | Status bar is dark, content doesn't overlap | ☐ |
| A4 | Check navigation bar | Nav bar is black, FAB not hidden behind it | ☐ |
| A5 | Toggle theme to Light | Theme switches cleanly, colors update | ☐ |
| A6 | Toggle theme back to Dark | Theme switches cleanly | ☐ |
| A7 | Scanline overlay visible (dark mode) | Subtle horizontal scanlines visible | ☐ |

### B. Home Screen & Lists
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| B1 | View seed data (3 starter lists) | Todos, Groceries, Ideas lists visible | ☐ |
| B2 | Tap list → navigate to detail | Smooth transition, detail screen opens | ☐ |
| B3 | Shared element animation | List card animates into detail header (or gracefully skipped) | ☐ |
| B4 | Back navigation | Returns to home, smooth transition | ☐ |
| B5 | Tap FAB (+) to add list | "New List" dialog appears | ☐ |
| B6 | Create a new list | List appears in home screen | ☐ |
| B7 | Long press to add list | "New List" dialog appears | ☐ |
| B8 | Swipe right on list → Edit | Edit dialog opens | ☐ |
| B9 | Edit list title and color | Changes saved correctly | ☐ |
| B10 | Swipe left on list → Delete | List is deleted | ☐ |
| B11 | Undo button after delete | List restored | ☐ |
| B12 | Sort menu → Sort A-Z | Lists sorted alphabetically | ☐ |
| B13 | Sort menu → Sort by Completion | Lists sorted by completion % | ☐ |
| B14 | Sort menu → Manual Order | Lists show in custom order | ☐ |
| B15 | Manual reorder (long press drag) | Lists can be reordered by dragging | ☐ |
| B16 | Swipe up on list → Duplicate | List duplicated with "Copy" suffix | ☐ |
| B17 | Swipe down on list → Add List | Add list dialog opens | ☐ |

### C. List Detail Screen
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| C1 | View items in a list | Items displayed with correct colors | ☐ |
| C2 | Tap FAB (+) to add item | "New Item" dialog appears | ☐ |
| C3 | Create new item | Item appears in list | ☐ |
| C4 | Double-tap item → toggle done | Done checkmark appears/disappears | ☐ |
| C5 | Swipe right on item → Edit | Edit dialog opens | ☐ |
| C6 | Edit item text and color | Changes saved correctly | ☐ |
| C7 | Swipe left on item → Delete | Delete confirmation appears | ☐ |
| C8 | Confirm delete item | Item removed from list | ☐ |
| C9 | Undo after item delete | Item restored | ☐ |
| C10 | Tap item to select | Item highlighted with color | ☐ |
| C11 | Select multiple items | Sum bar updates with selected items' sum | ☐ |
| C12 | Bottom sum bar | Shows total sum of numeric values in items | ☐ |
| C13 | Sort A-Z in detail | Items sorted alphabetically | ☐ |
| C14 | Manual order in detail | Items can be reordered | ☐ |
| C15 | Clear Completed | Done items removed | ☐ |
| C16 | Duplicate List (from menu) | New list created with items copied | ☐ |

### D. Search Screen
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| D1 | Navigate to Search | Search screen opens with text field | ☐ |
| D2 | Type search query | Matching lists and items shown | ☐ |
| D3 | Tap matched list | Navigates to that list detail | ☐ |
| D4 | Tap matched item | Navigates to parent list | ☐ |
| D5 | No matches | "NO MATCHES FOUND" text shown | ☐ |
| D6 | Back from search | Returns to home | ☐ |

### E. Settings Screen
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| E1 | Navigate to Settings | Settings screen opens | ☐ |
| E2 | NeonList branding visible | Logo, author name, version shown | ☐ |
| E3 | Theme toggle | Dark/light switch works | ☐ |
| E4 | Language → English | UI text changes to English | ☐ |
| E5 | Language → Italiano | UI text changes to Italian | ☐ |
| E6 | Language → සිංහල | UI text changes to Sinhala | ☐ |
| E7 | Export Backup | File picker opens, JSON saved | ☐ |
| E8 | Stats (lists/items count) | Correct counts displayed | ☐ |
| E9 | Credits section | Acknowledgments and license visible | ☐ |
| E10 | GitHub link | Opens browser to GitHub profile | ☐ |
| E11 | Back from settings | Returns to home | ☐ |

### F. Animations & Performance
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| F1 | Card entrance animations | Staggered slide-in animations on home | ☐ |
| F2 | Press scale animation | Cards shrink slightly on press | ☐ |
| F3 | Odometer sum text | Digits animate with odometer effect | ☐ |
| F4 | Horizontal swipe hints | Color overlay and text hint visible during swipe | ☐ |
| F5 | Vertical swipe hints | Duplicate/Add pill visible during long-press drag | ☐ |
| F6 | General performance | No noticeable lag or frame drops | ☐ |
| F7 | Memory usage | No OOM crashes after extended use | ☐ |

### G. Rotation & Configuration
| # | Test Case | Expected Result | Pass? |
|---|-----------|----------------|-------|
| G1 | Rotate to landscape | UI adapts, no crashes | ☐ |
| G2 | Rotate back to portrait | UI restores state | ☐ |
| G3 | Kill app process, reopen | Data persisted, app restores | ☐ |
| G4 | Keyboard open (editing) | Content visible above keyboard | ☐ |

---

## Also Test on API 35 (Regression Check)

After verifying API 29, run the same tests on an **API 35 emulator** to confirm:
- [ ] All existing behavior is preserved
- [ ] Splash screen still displays correctly
- [ ] No regressions

---

## Issue Tracking

If any test case fails, document it here:

| Test # | Issue Description | Severity | Fix Applied? |
|--------|-------------------|----------|-------------|
| | | | |

---

## Sign-Off

- [ ] All P0 tests pass on API 29
- [ ] All P0 tests pass on API 35 (regression)
- [ ] No crashes observed during 10+ minutes of usage
- [ ] Ready for PR review
