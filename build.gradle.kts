buildscript {
    configurations.classpath {
        // Keep AGP's build-only transitive dependencies above the advisory ranges.
        resolutionStrategy.force(
            "com.google.guava:guava:33.7.2-jre",
            "org.bouncycastle:bcprov-jdk18on:1.85",
            "org.bouncycastle:bcpkix-jdk18on:1.85",
            "org.bouncycastle:bcutil-jdk18on:1.85",
            "org.bitbucket.b_c:jose4j:0.9.6",
            "org.jdom:jdom2:2.0.6.1",
            "org.apache.commons:commons-lang3:3.18.0",
            "org.apache.httpcomponents:httpclient:4.5.14",
        )
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(25)
}

android {
    namespace = "com.anyremote.remote"
    // Compose 1.13 needs the 37.1+ platform; 37.2 is the newest one installed.
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "com.anyremote.remote"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "0.0.7"
    }

    flavorDimensions += "distribution"
    productFlavors {
        // GitHub releases: checks GitHub for new versions and installs them from the app.
        create("github") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "IN_APP_UPDATES", "true")
        }
        // F-Droid: no internet access at all. The F-Droid client handles updates.
        create("fdroid") {
            dimension = "distribution"
            buildConfigField("boolean", "IN_APP_UPDATES", "false")
        }
        create("play") {
            dimension = "distribution"
            buildConfigField("boolean", "IN_APP_UPDATES", "false")
        }
    }

    val keystorePassword =
        providers.environmentVariable("ANYREMOTE_KEYSTORE_PASSWORD").orNull
            ?: file("keystore.password").takeIf { it.exists() }?.readText()?.trim()

    signingConfigs {
        if (keystorePassword != null) {
            create("release") {
                storeFile = file("release.keystore")
                storePassword = keystorePassword
                keyAlias = "key0"
                keyPassword = keystorePassword
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    // AGP otherwise adds an encrypted "Dependency metadata" block, readable only by Google Play,
    // to the APK signing block. F-Droid rejects APKs that carry it, and it isn't needed elsewhere.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    if (providers.gradleProperty("android.experimental.enableScreenshotTest").orNull == "true") {
        testOptions {
            screenshotTests.create("screenshotTest") {
                engineVersion = libs.versions.screenshot.get()
                targetVariants.add("githubDebug")

                dependencies {
                    implementation(libs.androidx.compose.ui.tooling)
                    implementation(libs.screenshot.validation.api)
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    packaging {
        // This prebuilt AndroidX library is packaged as supplied.
        jniLibs.keepDebugSymbols += "**/libandroidx.graphics.path.so"
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Lint launches with its own tooling classpath, separate from the Gradle plugins.
configurations.configureEach {
    if (name == "androidLintTool") {
        val patchedModules =
            buildscript.configurations.getByName("classpath").resolutionStrategy.forcedModules.map {
                "${it.group}:${it.name}:${it.version}"
            }
        resolutionStrategy.force(*patchedModules.toTypedArray())
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
