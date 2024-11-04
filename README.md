# Demeter

![](screenshots/demeter.png =300x)

## Description

Demeter is a performance measurement library that could simplify performance issues investigation in your app. It's also useful to give a special prepared build with Demeter inside to your QA team to use it while regress process and upload performace report in the end.

## Features

- Tracer: Measures project methods with information about an execution thread, a consuming time and helps to sort by hazard level
- Inject: Wraps and calculates Injected constructors and its dependencies. It's useful to dig up problems with a dagger graph
- Enriches methods with profileable sections that helps to investigate problems in an Android Profiler or Perfetto
- Compose: observe StateObject changes and discover it
- Exports measurements to the excel format and Flipper

## Screenshots

![](screenshots/inject.png =300x)

Injected constuctor

![](screenshots/tracer.png =300x)

Tracer for methods

![](screenshots/export.png =600x)

Method export

![](screenshots/compose1.png =600x) ![](screenshots/compose2.png =600x)

Inspect Compose recompositions and ObjectStates

## Usage

Integrated Demeter represents an Activity that starts by notification click in any place of your application.

Demeter foundation consists of libraries:
- `demeter-core` - contains base interface without implementations. It also could be applied for release build type
- `demeter-profiler` - the main profiler implementation that should be applied for dev build type. You must check this artifact is not attached to release build type!
- - `base` - base profiler logic
- - `ui` - profiler ui
- `demeter-gradle-plugin` - main Demeter Gradle Plugin

Functionality is provided via specifying profiler plugins by consumer needs:
- `demeter-tracer-profiler-plugin` - tracing methods
- `demeter-inject-profiler-plugin` - analyzing @Inject constructor initialization
- `demeter-compose-profiler-plugin` - Jetpack Compose analyzer

For dev build type:
1. Add demeter plugin
```kotlin
plugins {
    id("com.yandex.demeter")
}
```
2. Add implementation of the profiler:
```kotlin
dependencies {
    // if buildType == Dev/Debug/etc...
    implementation("com.yandex.demeter:profiler")
}
```
3. Open your AndroidManifest and add:
```xml
<profileable
    android:enabled="true"
    android:shell="true"
    tools:ignore="UnusedAttribute" />
```
That helps Demeter to check and profile methods better. Do it only for Dev build types!

4. Add in your Application class:
```kotlin
override fun onCreate() {
    super.onCreate()
    ...
    Demeter.init(
        DemeterInitializer(
            context = this,
            flipperEnabled = false, // true - turn on flipper notifications
            profilerEnabled = false, // true - turn on trace section for profiler
        )
    )
    ...
}
```
5. Configure demeter plugin. You can apply only subplugins you need:
```kotlin
demeter {
    tracer {
        includedClasses = listOf("com.yandex.myapp") // list of packages that should be analyzed
    }

    inject {
        includedClasses = listOf("com.yandex.myapp") // list of packages that should be analyzed
        excludedClasses = listOf("com.yandex.myapp.excluded") // list of packages that shouldn't be analyzed
    }

    compose() // turn on compose inspections
}
```
6. Run and control!

## Gradle plugins

Demeter feature plugins can be applied via main plugin:
```kotlin
plugins {
    id("com.yandex.demeter")
}
```
or directly:
```kotlin
plugins {
    id("com.yandex.demeter.tracer")
    id("com.yandex.demeter.inject")
    ...
}
```

**Main plugin applies only plugins you specified in `demeter` block**

Feature plugins:
- `demeter-tracer-gradle-plugin`
- `demeter-inject-gradle-plugin`
- `demeter-compose-gradle-plugin`

## Troubleshooting
**Problem**: I get crash with stacktrace like
```
java.lang.AssertionError: Built-in class kotlin.Any is not found
```
**Solution**: Check you don't exclude `.kotlin_builtins` files from build
```kotlin
packagingOptions {
    resources {
        excludes += ['**/kotlin/**', '**.kotlin_builtins'] // or any other .kotlin_builtins exclude options
    }
}
```
