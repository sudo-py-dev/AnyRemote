# Security Policy

## Report a vulnerability

Use GitHub’s [private vulnerability reporting form](https://github.com/sudo-py-dev/AnyRemote/security/advisories/new). Please report security issues there before discussing them publicly.

Include the AnyRemote version or commit, Android version, affected feature, and the steps and conditions needed to observe the issue. Describe the impact and include relevant logs with credentials and personal information removed. Test only on devices and systems you’re authorized to use.

Start with the [latest signed release](https://github.com/sudo-py-dev/AnyRemote/releases/latest) when checking whether an issue still applies. Reports affecting the current `main` branch are also welcome.

## What to review

AnyRemote is an Android infrared remote. Remote control and saved settings are local to the device. Builds with the updater contact GitHub for release metadata and downloads. The policy covers the Android app, its bundled IR catalog, and the GitHub workflows that build, sign, and publish releases.

- **Updater.** Treat release metadata, download redirects, and APK bytes as untrusted inputs. Fetch through HTTPS from the configured GitHub sources, bound downloads, and check the checksum, package identity, and version before handing an APK to Android’s system installer. Android must still enforce signing checks and require user approval.
- **Android entry points.** Validate shortcut, widget, and Quick Settings requests before acting on them. Launcher shortcuts should only open saved remotes. Exported components and URI grants must expose only the operations and files they are intended to share. Opening the app from a shortcut or tile should not send an IR command.
- **Release pipeline.** Keep signing credentials out of source, logs, and uploaded artifacts. Limit repository write permission to publication. Release APKs must be signed, and their published checksums must match the files users download.

Prioritize issues that could bypass installation or signing checks, compromise releases, disclose private app data, or trigger unintended device actions. Describe the access and user interaction required so maintainers can assess the impact.
