# AnyRemote

A universal infrared remote for Android phones with a built-in IR blaster. Pick a device type and brand, confirm a code set with a quick test, and control TVs, air conditioners, fans, audio receivers, projectors, set-top boxes and cameras from one app.

Built with Kotlin and Jetpack Compose (Material 3).

## Features

- **Device-specific remotes**: TV and set-top box (D-pad, number pad, volume and channel), air conditioner, fan, A/V receiver, projector, camera shutter, plus a generic layout for everything else.
- **Full AC control**: temperature, mode, fan speed, auto sweep and fixed vane positions. Every press sends the complete state, the way real AC remotes do.
- **Code-set matching**: send a test signal, step through code sets until the device responds, and the app remembers the one that worked for that device and brand.
- **Favorites and renaming**: keep your remotes on the dashboard under your own names.
- **Home-screen shortcuts and widget**: pin a remote to the launcher, or use the quick widget for AC power and volume.
- **11 languages**: English, Hebrew, Arabic, Spanish, French, German, Italian, Portuguese, Russian, Chinese and Japanese, with right-to-left layout support.
- **Light, dark or system theme**, edge-to-edge drawing and haptic feedback that can be turned off.
- **Fully offline**: no network permission, no accounts, no tracking.

## Requirements

- A phone with a **built-in IR blaster** (`android.hardware.consumerir`). Many Xiaomi, Redmi and POCO phones and some Huawei and Honor models have one. On phones without one, the app opens but warns that it can't transmit.
- Android 8.0 (API 26) or newer.

## Project status

AnyRemote is in early development (version 0.0.1).

- **Air conditioners.** Search for Tadiran or תדיראן for the Gree wall-mounted profile. Tadiran documents a different setup for mini-central units in its [Smart IR guide](https://www.tadiran-group.co.il/wp-content/uploads/2020/12/Tadiran-IOT-and-Smart-IR-instructions-V01.pdf). Electra Classic INV 17 / AXW12DCS, AUX YKR-T/011, and Amcor ADR-853H / TAC-444 / TAC-495 have dedicated encoders based on the [IRremoteESP8266 protocol reference](https://github.com/crankyoldgit/IRremoteESP8266/blob/master/SupportedProtocols.md). Other legacy AC entries use Gree. Confirm compatibility on the appliance during setup.
- **Model-specific TV and streaming controls.** The [TCL 32S327](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/TVs/TCL/TCL_32S327.ir), [LG TV NEC](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/TVs/LG/LG.ir), and [onn. 4K Pro](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/Streaming_Devices/onn/onn_4K_Pro.ir) entries send captured commands. Select the named profile to use them. Other non-AC catalog entries retain the generic NEC matching flow.

## Editing the remote catalog

Edit `src/main/assets/remote.json` to add devices or brands. Each brand has a numeric `id`, a display name (`nameEn`), and a `category` used for search ranking. Keep existing device and brand IDs stable because favorites and launcher shortcuts use them. Model-specific IR commands live in the Kotlin codebook.

## TV converters and receivers

Find these profiles under **Set-top box**. Pairing sends a model-specific power signal. Confirm that your receiver responds before saving the remote.

| Profile | IR capture |
| --- | --- |
| Apex Digital DT250 | [LIRC DT250](https://github.com/probonopd/lirc-remotes/blob/master/remotes/apex/DT250.lircd.conf) |
| Diprogress DVB-T | [Diprogress capture](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/DVB-T/Diprogress/Diprogress_dvb_t.ir) |
| Octagon SF8008 | [SF8008 capture](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/DVB-T/Octagon/Octagon_SF8008.ir) |
| Storex MediaZapper HD | [MediaZapper HD capture](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/DVB-T/Storex/MediaZapperHD.ir) |
| Tesla TE-300 | [TE-300 capture](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/DVB-T/Tesla/Tesla_TE-300.ir) |
| Thomson THT501 | [THT501 capture](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/Cable_Boxes/Thomson/Thomson_THT501.ir) |
| Tilview DVB-T2 | [Tilview capture](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/DVB-T/Tilview/TILVIEW_DVB-T_2.ir) |

## Building

You need JDK 17 and the Android SDK with platform 37.2 installed.

```bash
git clone https://github.com/sudo-py-dev/AnyRemote.git
cd AnyRemote
./gradlew assembleDebug
```

The APK is written to `build/outputs/apk/debug/`. To install it on a connected phone:

```bash
./gradlew installDebug
```

Run the unit tests:

```bash
./gradlew testDebugUnitTest
```

### Release builds

`./gradlew assembleRelease` produces a minified build. It is signed only when a password is available, either from the `ANYREMOTE_KEYSTORE_PASSWORD` environment variable or a `keystore.password` file in the project root, together with `release.keystore` (key alias `key0`). Without them you get an unsigned APK. Keystore and password files are git-ignored; never commit them.

## Project layout

```
src/main/kotlin/com/anyremote/remote/
├── MainActivity.kt        Entry point, splash screen, type-safe navigation
├── data/                  Loads the device and brand database (remote.json)
├── ir/                    IR encoding and transmission
│   ├── RemoteKey.kt       Logical buttons, each with a unique command
│   ├── IrCodebook.kt      Device + code set + key → NEC frame
│   ├── AcState.kt         Full AC state, encoded as one Gree frame
│   ├── IREncodingEngine.kt  Gree and NEC frame builders
│   └── IRTransmitter.kt   Sends frames through ConsumerIrManager off the main thread
├── service/               Favorites and settings, locale, launcher shortcuts
├── ui/
│   ├── components/        Shared remote buttons (RemoteKeys.kt), brand and category pickers
│   ├── layouts/           One remote layout per device type
│   ├── screens/           Dashboard, match wizard, remote, settings, edit
│   └── theme/             Colors and Material theme
└── widget/                Home-screen widget and its button receiver
src/test/                  Unit tests for IR encoding
```

Dependencies are managed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Contributing

Issues and pull requests are welcome, especially:

- verified IR codes for specific brands and models
- reports of which AC models respond to the Gree frames
- new translations or corrections

Please run `./gradlew testDebugUnitTest assembleDebug` before opening a pull request.

## License

AnyRemote is released under the [MIT License](LICENSE).
