# Submitting AnyRemote to F-Droid

F-Droid builds the `fdroid` flavor from source at a release tag, signs it with its own key and publishes it. [`com.anyremote.remote.yml`](com.anyremote.remote.yml) is the build recipe. The store listing (title, descriptions, changelogs) comes from [`fastlane/metadata/android/`](../../fastlane/metadata/android/en-US) in this repository, so F-Droid picks up changes there with each release.

## Before you submit

1. **Tag a release that includes the flavor.** `v0.0.4` was tagged before the flavor existed. Bump to `versionName = "0.0.5"` and `versionCode = 5`, merge, and push the `v0.0.5` tag. The recipe builds `commit: v0.0.5`.
2. **Optional: check the build the way F-Droid does.** With Docker, follow F-Droid's [build metadata guide](https://f-droid.org/docs/Build_Metadata_Reference/) to run `fdroid build -v -l com.anyremote.remote` in the `fdroidserver` image.

The build uses JDK 25 (`jvmToolchain(25)`), which F-Droid's Debian build server has.

## Submit

1. Sign in to [GitLab](https://gitlab.com) and fork [fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata).
2. In your fork, create a branch named `com.anyremote.remote`. Add the recipe as `metadata/com.anyremote.remote.yml`.
3. Open a merge request to `fdroid/fdroiddata` using the **App inclusion** template, and fill in its checklist. Here's a description you can use:

   > AnyRemote turns an Android phone with a built-in IR blaster into a remote for TVs, air conditioners, fans, projectors and more.
   >
   > - License: MIT. All dependencies are AndroidX, Jetpack Compose and Kotlin libraries from Google Maven and Maven Central, with no proprietary components.
   > - F-Droid builds the `fdroid` product flavor. It has no `INTERNET` or `REQUEST_INSTALL_PACKAGES` permission, and its in-app updater is switched off at build time (`BuildConfig.IN_APP_UPDATES = false`), so F-Droid handles updates.
   > - The only permissions are `TRANSMIT_IR` and `VIBRATE`.
   > - Store metadata is in `fastlane/metadata/android/` in the source repository.
   > - Updates are found from version tags (`UpdateCheckMode: Tags`).

4. The F-Droid CI pipeline builds the app on the merge request. Fix anything it reports, and answer reviewers in the merge request.

## Things to know

- **Different signing keys.** F-Droid signs with its own key, so a GitHub build can't be updated with an F-Droid build, or the other way round. Users have to uninstall first, which deletes their saved remotes. To share one key, you'd need [reproducible builds](https://f-droid.org/docs/Reproducible_Builds/) and the expected signature added to the recipe. That's a later step.
- **Category.** The recipe uses `Connectivity`. Check F-Droid's current [category list](https://gitlab.com/fdroid/fdroiddata/-/blob/master/config/categories.yml) for a closer match. A reviewer may also suggest one.
- **Updates after inclusion.** With `AutoUpdateMode: Version` and `UpdateCheckMode: Tags`, every new `vX.Y.Z` tag with a higher `versionCode` gets built and published automatically. Add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` for each release.
