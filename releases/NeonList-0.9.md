# NeonList 0.9 Release Notes

Release date: 2026-02-03

Highlights:
- Vertical Gesture Overhaul: Vertical dragging for "Add New" and "Duplicate" now requires a long-press, removing conflicts with normal list scrolling. Neighboring items create animated gaps for a more fluid feel.
- Feature Parity: Home and List Detail screens now share the same interactions (Edit, Delete, Add, Duplicate) and Undo support.
- Comprehensive Undo: Undo now tracks name/color changes in addition to deletions and completion toggles.
- Architectural Cleanup: Multi-axis swipe logic centralized into the reusable `SwipeActions` component for consistent behavior.
- Polished Animations: `AnimatedVisibility` and `animateDpAsState` used for smoother swipe/drag transitions.