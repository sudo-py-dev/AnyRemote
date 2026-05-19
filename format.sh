#!/usr/bin/env bash

# Silence JVM Unsafe and native-access warnings in JDK 21+
export JDK_JAVA_OPTIONS="--sun-misc-unsafe-memory-access=allow --enable-native-access=ALL-UNNAMED"

echo "🧹 Auto-formatting all Kotlin files recursively..."
java -jar ktfmt.jar --kotlinlang-style $(find src -name "*.kt")

echo "✅ Codebase formatting successfully completed!"
