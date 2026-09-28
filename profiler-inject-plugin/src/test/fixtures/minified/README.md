# Minified inject smoke test

Run from the Demeter root with Android SDK and Java 17 configured:

```sh
./gradlew -I profiler-inject-plugin/src/test/fixtures/minified/include.init.gradle :injectValidation:assembleRelease --no-configuration-cache
adb install -r profiler-inject-plugin/src/test/fixtures/minified/build/outputs/apk/release/injectValidation-release.apk
adb shell am start -n sample.demeter.injectvalidation/sample.demeter.MainActivity
adb logcat -d -s DemeterInjectValidation:I AndroidRuntime:E
```

The app must display/log `PASS: 7 inject events after R8`. It uses the real inject
Gradle plugin, the local runtime, and R8 full optimization/obfuscation without
keep rules for fixture constructors or Kotlin metadata. Check the generated
`build/outputs/mapping/release/mapping.txt`: `sample.demeter.PreferencesProvider`
should be renamed or merged. It must still appear by its original name in metrics.

The fixture covers repeated construction, primitive constructor parameters,
dependency links, distinct base/derived constructor owners, and the legacy ASM
entry point. It is opt-in and is not included in normal publication builds.
