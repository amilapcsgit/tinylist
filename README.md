<div align="center">

<img src="screenshots/playstore/neonlist-splash.png" alt="NeonList offline and backup preview" width="100%"/>

# NeonList

**Free. Offline. Yours forever.**

NeonList is an offline Android list manager built for people who need fast lists, quick numeric totals, and confident local backup without accounts, ads, or cloud dependency.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Offline](https://img.shields.io/badge/100%25-Offline-00F5FF?style=for-the-badge)](#privacy--data)
[![Release](https://img.shields.io/badge/Latest-v1.5-98E650?style=for-the-badge)](https://github.com/amilapcsgit/NeonList/releases/tag/v1.5)

**Package name:** `com.pcslanka.neonlist`  
**Status:** Release candidate prepared for Google Play publication

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

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/amilapcsgit/NeonList)
[![Twitter](https://img.shields.io/badge/Twitter-1DA1F2?style=for-the-badge&logo=twitter&logoColor=white)](https://twitter.com)
[![Website](https://img.shields.io/badge/Website-00D9FF?style=for-the-badge&logo=google-chrome&logoColor=white)](https://amilaprasad.it)

*© 2026 L.J. Amila Prasad Perera. All rights reserved. Built with passion.*

</div>

---

## Why NeonList

NeonList is not just another to-do list. It is a small, fast, offline-first utility for managing lists where numbers matter.

Use it for shopping totals, cash tracking, packing, luggage balancing, quick inventory counts, ideas, tasks, or any list where you want the app to calculate totals while you type naturally.

Example:

```text
PM blue bag 23.4 KG
PM black bag 27.2 KG
Carpisa orange 29.2 KG
```

NeonList extracts the final number in each item and shows the total instantly. Tap selected items to calculate only those rows.

---

## ✨ Features

NeonList isn't just a list manager; it's a statement. Designed with a **Cyberpunk UI**, it leverages **Material 3** and **Jetpack Compose** to deliver a fluid, haptic-rich experience.

- 💎 **Neon Aesthetics**: High-contrast, vibrant visuals that pop
- 🌍 **Global Access**: Support for English, Italian, and Sinhala
- 🛡️ **Zero Trace**: 100% offline-first storage with Room
- ⚡ **Flow UX**: Gesture-driven interactions for power users
- 🎯 **Smart Summation**: Auto-extracts numeric values for real-time aggregation
- 🔄 **Temporal Undo**: Multi-stack history rollback for all data mutations


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

<img src="screenshots/architecture_diagram.jpg" alt="Architecture Diagram" width="600"/>

---

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

---

![Made with Love](https://img.shields.io/badge/Made%20with-💜-purple?style=for-the-badge)
![Powered by Kotlin](https://img.shields.io/badge/Powered%20by-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Built with Compose](https://img.shields.io/badge/Built%20with-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)


<div align="center">

**NeonList**  
Offline lists. Instant totals. Local backup.
*Optimized by a person with 25 year experiance in IT support for real-world use*
</div>
