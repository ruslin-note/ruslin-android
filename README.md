<div align="center">
<div align="center">
    <h1>Ruslin</h1>
    <p>A simple notes application that supports syncing notes using a self-hosted Joplin server.</p>
    <p>English by DeepL&nbsp;&nbsp;|&nbsp;&nbsp;<a target="_blank" href="./README-zh-CN.md">简体中文</a></p>
</div>

[![Build](https://github.com/ruslin-note/ruslin-android/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/ruslin-note/ruslin-android/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/ruslin-note/ruslin-android)](https://github.com/ruslin-note/ruslin-android/blob/main/LICENSE)
[![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/ruslin-note/ruslin-android?include_prereleases&label=preview&logo=github)](https://github.com/ruslin-note/ruslin-android/releases)
[![Downloads](https://img.shields.io/github/downloads/ruslin-note/ruslin-android/total)](https://github.com/ruslin-note/ruslin-android/releases)
[![Mastodon Follow](https://img.shields.io/mastodon/follow/109781051461798350?domain=https%3A%2F%2Ffosstodon.org&style=social)](https://fosstodon.org/@ruslin)

<div align="center">
    <img src="./fastlane/metadata/android/zh-CN/images/notes.png" width="19.2%" alt="notes" />
    <img src="./fastlane/metadata/android/zh-CN/images/folders.png" width="19.2%" alt="folders" />
    <img src="./fastlane/metadata/android/zh-CN/images/editor.png" width="19.2%" alt="editor" />
    <img src="./fastlane/metadata/android/zh-CN/images/search.png" width="19.2%" alt="search" />
    <img src="./fastlane/metadata/android/zh-CN/images/account.png" width="19.2%" alt="account" />
    <br/>
    <br/>
</div>
</div>

🚧 Currently in Pre-alpha, not ready for use in production environments. Please be careful to back up. 🚧

Supported features:

- ✅ Support Markdown edit and preview
- ✅ Full-text search using jieba-rs (Chinese and English supported)
- ✅ Sync notes using a self-hosted Joplin server
- ✅ Manual and automatic synchronization
- 🚧 Possible compatibility with Joplin's sync format (End-to-end encryption is not supported)

## Download

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/org.dianqk.ruslin/)
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
    alt="Get it on Google Play"
    height="80" />](https://play.google.com/store/apps/details?id=org.dianqk.ruslin)
[<img src=".github/get-it-on-github.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/DianQK/ruslin-android/releases)
 or [nightly](https://github.com/ruslin-note/ruslin-android/releases/tag/nightly).

> Ruslin is a reproducible build of app, you don't need to worry about F-Droid and other store signature issues, see: [Towards a reproducible F-Droid](https://f-droid.org/en/2023/01/15/towards-a-reproducible-fdroid.html).

## Build

The following instructions are based on a Linux development environment and an arm64 physical device for debugging.

### Requirements

- [Rust 1.75.0](https://www.rust-lang.org/tools/install)
- [Android Studio](https://developer.android.com/studio)
- [NDK 26.1.10909125](https://developer.android.com/ndk/downloads)

### Build Instructions

#### 1. You need to set the NDK environment variable.

Example:

```shell
export ANDROID_HOME=$HOME/Android/Sdk
export NDK_VERSION=26.1.10909125
```

#### 2. Build the Rust library.

> You can learn about how Kotlin interoperates with Rust from [uniffi-rs](https://github.com/mozilla/uniffi-rs).

Run `build.sh` in the `ruslin-data-uniffi` directory.

```shell
cd ruslin-data-uniffi
./build.sh
```

#### 3. Build the apk.

```shell
./gradlew :app:assembleDebug
```

> For more build details, refer to [Github Actions](.github/workflows).

## Credits

- [Joplin](https://github.com/laurent22/joplin): [AGPL-3.0](https://github.com/laurent22/joplin/blob/dev/LICENSE)
- [ReadYou](https://github.com/Ashinch/ReadYou): [GPL-3.0](https://github.com/Ashinch/ReadYou/blob/main/LICENSE)
- [Seal](https://github.com/JunkFood02/Seal): [GPL-3.0](https://github.com/JunkFood02/Seal/blob/main/LICENSE)

## License

[GNU GPL v3.0](https://github.com/DianQK/ruslin-android/blob/main/LICENSE)
