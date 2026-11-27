<div align="center">

<img src="docs/assets/anyremote-icon.svg" width="112" height="112" alt="AnyRemote app icon">

# AnyRemote

**Turn your Android phone into a remote for your TV, air conditioner, fan, projector and more.**

[![Build](https://img.shields.io/github/actions/workflow/status/sudo-py-dev/AnyRemote/build.yml?branch=main&label=build&logo=github)](https://github.com/sudo-py-dev/AnyRemote/actions/workflows/build.yml)
[![CodeQL](https://img.shields.io/github/actions/workflow/status/sudo-py-dev/AnyRemote/codeql.yml?branch=main&label=CodeQL&logo=github)](https://github.com/sudo-py-dev/AnyRemote/actions/workflows/codeql.yml)
[![Latest release](https://img.shields.io/github/v/release/sudo-py-dev/AnyRemote?label=release&color=blue)](https://github.com/sudo-py-dev/AnyRemote/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/sudo-py-dev/AnyRemote/total?color=blue)](https://github.com/sudo-py-dev/AnyRemote/releases)
[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#requirements)
[![License: MIT](https://img.shields.io/github/license/sudo-py-dev/AnyRemote)](LICENSE)

<a href="https://github.com/sudo-py-dev/AnyRemote/releases/latest">
  <img src="https://img.shields.io/badge/Download_APK-latest-2ea44f?style=for-the-badge&logo=android&logoColor=white" alt="Download the latest APK" height="44">
</a>

<sub>Free · Open source · No ads · No account · Works offline</sub>

</div>

---

## ✨ Features

| | |
|---|---|
| 📺 **Many device types** | TVs, set-top and streaming boxes, air conditioners, fans, A/V receivers, projectors, DVD players, cameras and more. |
| 🔍 **Find your code fast** | Pick a brand or model, tap the test button, and step through code sets until your device responds. |
| ⭐ **Saved remotes** | Save, rename and favorite the remotes you use every day. |
| ⚡ **Quick access** | Home screen widgets and shortcuts, plus a Quick Settings tile. |
| 🎨 **Looks like your phone** | Light and dark themes, and Material You dynamic colors on Android 12 and later. |
| 🌍 **11 languages** | English, Arabic, Chinese, French, German, Hebrew, Italian, Japanese, Portuguese, Russian and Spanish. |
| 🔄 **Built-in updates** | Checks GitHub for new versions and verifies each download's checksum before installing. |
| 🐞 **Crash reports you control** | If the app crashes, you see exactly what the report contains and choose whether to send it. |

## 📋 Requirements

- **Android 8.0** or later
- A phone with a built-in **infrared (IR) blaster**. Many Xiaomi, Redmi, POCO, Huawei and Honor phones have one. Check your phone's specifications if you're not sure.

## 🚀 Get started

1. **[Download the latest APK](https://github.com/sudo-py-dev/AnyRemote/releases/latest)** from **Assets** on the release page.
2. Open the file on your phone and follow Android's installation prompt. You may need to allow installs from your browser or file manager.
3. In AnyRemote, tap **Add remote** and pick your device type and brand.
4. Point your phone at the device and tap the test button. If the device responds, save the remote. If not, try the next code.

After that, the app tells you when a new version is out, and you can install it right from the app.

> [!TIP]
> Add your favorite remote to your home screen, or go to **Settings → Add to Quick Settings**, to open it in one tap.

## 🔒 Privacy

AnyRemote doesn't collect any data, and it has no ads, trackers or account.

- Remote controls run entirely on your phone and work without an internet connection.
- The internet is only used to check GitHub for app updates and to download them.
- Crash reports stay on your phone unless you choose to send one. A report has your app version, Android version, phone model and the error trace. Personal details such as links, file paths and names are removed from error messages.

## 🤝 Help and feedback

Compatibility depends on the exact model. Choose the form that fits:

- 📡 **[Request a remote](https://github.com/sudo-py-dev/AnyRemote/issues/new?template=remote_request.yml)** if your brand or model is missing or doesn't respond.
- 🐛 **[Report a bug](https://github.com/sudo-py-dev/AnyRemote/issues/new?template=bug_report.yml)** if something doesn't work right.
- 💡 **[Suggest a feature](https://github.com/sudo-py-dev/AnyRemote/issues/new?template=feature_request.yml)** to share an idea.
- 🔐 **[Report a security issue](SECURITY.md)** privately, not in a public issue.

## 🛠️ For developers

AnyRemote is written in Kotlin with Jetpack Compose. To build it, contribute or add remotes, see:

- [Developer guide](docs/development.md)
- [Remote catalog notes](docs/missing-ir-brands.md)

## 📄 License

AnyRemote is released under the [MIT License](LICENSE).
