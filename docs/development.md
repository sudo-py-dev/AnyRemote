# Developing AnyRemote

App, test, and screenshot preview code use Kotlin. The Gradle build uses Kotlin DSL. Android resources and the IR catalog use XML and JSON.

## Build and check

Use JDK 27 and Android SDK platform 37.2. The project targets Java 17 bytecode for Android compatibility.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The APK is in `build/outputs/apk/debug/`. To install it on a connected device, run `./gradlew installDebug`.

Download the standalone jar for [ktfmt 0.64](https://kotlin.github.io/ktfmt/). Point `KTFMT_JAR` at it, or save it as `ktfmt.jar` in the repository root.

```bash
./format.sh
./format.sh --check
```

## Source layout

- `src/main/kotlin/com/anyremote/remote/` contains the app entry point, screens, catalog, IR encoders, platform integrations, and updater.
- `src/main/res/` contains Android resources and translations.
- `src/main/assets/remote.json` contains the local remote catalog.
- `src/test/kotlin/` contains unit tests. `src/screenshotTest/kotlin/` contains Compose previews for screenshot checks.
- `gradle/libs.versions.toml` contains dependency versions.

### Release builds

`./gradlew assembleRelease` produces a minified build. It is signed only when a password is available, either from the `ANYREMOTE_KEYSTORE_PASSWORD` environment variable or a `keystore.password` file in the project root, together with `release.keystore` (key alias `key0`). Without them you get an unsigned APK. Keystore and password files are git-ignored; never commit them.

## Editing the remote catalog

See the [missing IR brands inventory](missing-ir-brands.md) for candidates and example captures.

Edit `src/main/assets/remote.json` to add devices or brands. Each brand has a numeric `id`, a display name (`nameEn`), and a `category` used for search ranking. Keep existing device and brand IDs stable because favorites and launcher shortcuts use them. Add alternate names in `aliases` and model-specific signals in `ir`. For NEC, supply `address` and `secondAddress` as decimal bytes and map logical button names such as `POWER` to command bytes in `keys`. Pairing uses `POWER` by default. Set `testKey` when the remote uses a different button for pairing. For AC profiles, use `GREE`, `ELECTRA`, or `AMCOR` as the protocol. Kotlin handles signal encoding.

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

## Profile compatibility notes

- **Air conditioners.** Search for Tadiran or תדיראן for the Gree wall-mounted profile. Tadiran documents a different setup for mini-central units in its [Smart IR guide](https://www.tadiran-group.co.il/wp-content/uploads/2020/12/Tadiran-IOT-and-Smart-IR-instructions-V01.pdf). Electra Classic INV 17 / AXW12DCS, AUX YKR-T/011, and Amcor ADR-853H / TAC-444 / TAC-495 have dedicated encoders based on the [IRremoteESP8266 protocol reference](https://github.com/crankyoldgit/IRremoteESP8266/blob/master/SupportedProtocols.md). Other legacy AC entries use Gree. Confirm compatibility on the appliance during setup.
- **Model-specific TV and streaming controls.** The [TCL 32S327](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/TVs/TCL/TCL_32S327.ir), [LG TV NEC](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/TVs/LG/LG.ir), and [onn. 4K Pro](https://github.com/Lucaslhm/Flipper-IRDB/blob/main/Streaming_Devices/onn/onn_4K_Pro.ir) entries send captured commands. Select the named profile to use them. Other non-AC catalog entries retain the generic NEC matching flow.
