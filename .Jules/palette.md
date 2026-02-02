## 2025-05-15 - [Accessible Color Selection Grid]
**Learning:** Using `Modifier.selectable` with `Role.RadioButton` provides much better accessibility than simple tap gestures for selection-based components. Explicit `contentDescription` for color swatches and a visual `Check` icon significantly improve the experience for both screen reader and sighted users.
**Action:** Always use semantic selection modifiers (`selectable`, `toggleable`) instead of raw tap gestures for UI choices, and ensure every visual choice has a textual equivalent and clear visual state indicator.
