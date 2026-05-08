<div align="center">

<img src="screenshots/playstore/neonlist-splash.png" alt="NeonList offline and backup preview" width="100%"/>

# NeonList

**Free. Offline. Fast Todo-list with instant totals.**

NeonList is a free offline Android list manager built for people who need fast to-do lists, quick numeric totals, and confident local JSON backup without accounts, ads, subscriptions, or cloud dependency.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Offline](https://img.shields.io/badge/100%25-Offline-00F5FF?style=for-the-badge)](#privacy--data)
[![Release](https://img.shields.io/badge/Latest-v1.5-98E650?style=for-the-badge)](https://github.com/amilapcsgit/NeonList/releases/tag/v1.5)

**Package name:** `com.pcslanka.neonlist`  
**Status:** Release candidate prepared for Google Play publication

**Keywords:** todo, to-do list, offline to-do app, offline-first Android app, numeric list, list calculator, shopping total, weight total, packing list, local JSON backup

</div>

---

## Preview

<table>
  <tr>
    <td width="50%" align="center">
      <img src="screenshots/playstore/neonlist-hero.png" alt="NeonList hero image" width="100%"/>
    </td>
    <td width="50%" align="center">
      <img src="screenshots/playstore/neonlist-tutorial.png" alt="NeonList gesture tutorial preview" width="100%"/>
    </td>
  </tr>
</table>

---

<div align="center">

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/amilapcsgit/NeonList)
[![Website](https://img.shields.io/badge/Website-00D9FF?style=for-the-badge&logo=google-chrome&logoColor=white)](https://amilaprasad.it)

Copyright (c) 2026 L.J. Amila Prasad Perera. All rights reserved.

</div>

---

## Why NeonList

Most to-do apps are slow for real-world counting. They are built around accounts, sync, projects, labels, and workflows when sometimes you just need a fast list that also does the math.

NeonList is a small, fast, offline-first to-do list and numeric list calculator for managing lists where numbers matter.

Use it for shopping totals, cash tracking, packing, luggage balancing, quick inventory counts, ideas, tasks, or any list where you want the app to calculate total cost, total weight, total quantity, or selected subtotals while you type naturally.

Example:

```text
PM blue bag 23.4 KG
PM black bag 27.2 KG
Carpisa orange 29.2 KG
```

NeonList extracts the final number in each item and shows the total instantly. Tap selected items to calculate only those rows, so you can quickly compare costs, weights, quantities, or groups without opening a spreadsheet.

---

## ✨ Features

NeonList keeps the speed of a simple to-do list and adds the one thing traditional list apps usually miss: instant calculations.

- **Free to use:** built as a practical offline utility, not a subscription funnel.
- **Fast to-do lists:** create, edit, complete, duplicate, delete, sort, and search quickly.
- **List calculator:** auto-extract numeric values for total cost, total weight, total quantity, and quick subtotals.
- **Selected sums:** tap only the rows you want and see the selected total immediately.
- **100% offline-first:** local Room/SQLite storage, no login, no internet permission.
- **Local JSON backup:** export and import plain JSON files through Android's system file picker.
- **Gesture workflow:** swipe, double tap, and hold-drag actions for fast repeated use.
- **Multilingual UI:** English, Italian, and Sinhala.
- **Cyberpunk Material 3 UI:** polished Jetpack Compose interface with dark and light neon themes.

---

## Gesture Guide

| Gesture | Action |
| --- | --- |
| Tap item | Select for selected sum |
| Double tap item | Mark done |
| Swipe right | Edit list or item |
| Swipe left | Delete list or item |
| Hold + drag down | Add near the row |
| Hold + drag up | Duplicate list or item |
| Manual order mode | Drag handles to reorder |

Normal vertical drag still scrolls. Add and duplicate require **hold + drag**.

---

## Privacy & Data

NeonList is designed to keep your data on your device.

- No login.
- No account.
- No internet permission.
- No contacts, location, camera, microphone, or storage permission.
- App data is stored locally with Room/SQLite.
- Export uses Android's system document picker and writes only to the location you choose.
- Backup files are plain JSON so you can keep your own copies.

---

## Download

NeonList is being prepared for Google Play. Until the Play Store listing is live, release builds are available from GitHub.

- **Latest APK:** [NeonList-1.5.apk](https://github.com/amilapcsgit/NeonList/releases/download/v1.5/NeonList-1.5.apk)
- **Release notes:** [releases/NeonList-1.5.md](releases/NeonList-1.5.md)
- **All releases:** [github.com/amilapcsgit/NeonList/releases](https://github.com/amilapcsgit/NeonList/releases)

> Note: APK sideloading may require allowing installs from your browser or file manager. Google Play distribution is the intended public release path.

---

## Build From Source

### Requirements

- Android Studio
- JDK 17
- Android SDK 35
- Android 10+ target device or emulator

### Commands

```powershell
git clone https://github.com/amilapcsgit/NeonList.git
cd NeonList/android

.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Release builds require the signing keystore values to be supplied through local properties or environment variables. Signing secrets are not stored in this repository.

---

### 📊 Architecture Overview
<div align="center">
<img src="screenshots/architecture_diagram.jpg" alt="Architecture Diagram" width="600"/>
</div>

## Technical Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM with StateFlow
- **Database:** Room / SQLite
- **Serialization:** Kotlinx Serialization
- **Minimum Android:** Android 10, API 29
- **Current app ID:** `com.pcslanka.neonlist`
- **Internal Kotlin namespace:** `com.cyberlist.neonlist`

Keeping the internal namespace separate from the Play Store app ID avoids unnecessary source-package churn while publishing under the final package name.

---

## Project Structure

```text
android/
  app/src/main/java/com/cyberlist/neonlist/
    data/          Room entities, DAOs, repository, import/export
    ui/            Compose theme, localization, app navigation
    ui/screens/    Home, list detail, search, settings
    ui/components/ Reusable controls and gesture components

docs/dev/          LLM and developer support notes
releases/          Versioned APK/AAB release artifacts and notes
screenshots/       App screenshots and Play Store preview assets
```

---

## 📸 Screenshots

<details>
<summary>📂 <b>CLICK TO EXPAND SCREENSHOTS</b></summary>

| Home Overview | Edit Action | Delete Action |
|:---:|:---:|:---:|
| ![Home overview](screenshots/home_lists_overview.jpg) | ![Home edit action](screenshots/home_action_edit.jpg) | ![Home delete action](screenshots/home_action_delete.jpg) |

| Duplicate Action | List Detail | List Menu |
|:---:|:---:|:---:|
| ![Home duplicate action](screenshots/home_action_duplicate.jpg) | ![List detail items](screenshots/list_detail_items.jpg) | ![List detail menu](screenshots/list_detail_menu.jpg) |

| Settings Screen |
|:---:|
| ![Settings screen](screenshots/settings_screen.jpg) |

</details>

---

## Roadmap

- Publish NeonList on Google Play.
- Continue polishing onboarding and store screenshots.
- Add more explicit first-run hints for selected sums and hold-drag gestures.
- Keep the app offline-first and simple by default.

---

## 📄 License

This software is protected by a **Custom License**. It is **Free for Personal Use** but restricted for commercial distribution. See the [LICENSE](LICENSE) file for details.

```text
Copyright (c) 2026 L.J. Amila Prasad Perera

1. NON-COMMERCIAL USE ONLY
2. NO REDISTRIBUTION
3. ATTRIBUTION REQUIRED
4. NO WARRANTY
```

---

## 🙏 Acknowledgments

Special thanks to:

- **Jetpack Compose Team** - For the amazing UI toolkit
- **Material Design** - For the design system
- **Android Community** - For continuous support and inspiration
- **Contributors** - Everyone who has contributed to this project

**Technologies Used:**
- [Kotlin](https://kotlinlang.org/) - Programming language
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI framework
- [Room](https://developer.android.com/training/data-storage/room) - Database
- [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async programming
- [Material 3](https://m3.material.io/) - Design system

---

## 📞 Contact

- 📧 **Email**: [amilapcsgit@gmail.com](mailto:amilapcsgit@gmail.com)
- 💬 **Issues**: [GitHub Issues](https://github.com/amilapcsgit/Neonlist/issues)
- 🌐 **Website**: [amilaprasad.it](https://amilaprasad.it)


---

<div align="center">

### ⚡ Built with Passion ⚡

Made with 💜 by **L.J. Amila Prasad Perera** | Powered by Kotlin & Jetpack Compose

![Made with Care](https://img.shields.io/badge/Made%20with-Care-purple?style=for-the-badge)
![Powered by Kotlin](https://img.shields.io/badge/Powered%20by-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Built with Compose](https://img.shields.io/badge/Built%20with-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

**NeonList**  
Offline lists. Instant totals. Local backup.

*Optimized by a person with 25 years of IT support experience for real-world use.*

</div>
