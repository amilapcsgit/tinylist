# NeonList 1.1 Release Notes

Release date: 2026-02-16

Highlights:
- Android 10 support: minimum SDK lowered to API 29, expanding device compatibility.
- Splash screen failsafe: API 31-only `windowSplashScreen*` attributes moved to `values-v31` to prevent Android 10 crashes.
- Theming compatibility: base theme kept API 29-safe while preserving full splash behavior on API 31+.
- Stability validation: Android 10 compatibility changes compiled, linted, and smoke-tested successfully.
- Manual verification: confirmed working on three Android 10 phones before release.
