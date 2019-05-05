# FluctSDK-Kotlin

An unofficial [Kotlin Multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) bindings library of [FluctSDK](https://github.com/voyagegroup/FluctSDK-iOS).  
This Library supports "Rewarded Video Ads" feature on [Kotlin MPP Project (iOS, Android)](./examples/mpp) and [Pure Android app](./examples/android).

## Disclaimer

"FluctSDK" and their I/F are the property of [fluct, Inc](https://corp.fluct.jp).
This library is developed to based on the [published official document](https://github.com/voyagegroup/FluctSDK-iOS/wiki).

## Usages

Enable "Gradle Module Metadata" feature in `settings.gradle`:

```groovy
enableFeaturePreview('GRADLE_METADATA')
```

Add repository:

```groovy
repositories {
    // FluctSDK-Kotlin
    maven { url 'https://s64.github.io/FluctSDK-Kotlin/m2' }
}
```

Add module in `common` sourceSet:

```groovy
sourceSets {
    commonMain {
        dependencies {
            implementation 'jp.s64.kotlin:fluctsdk:0.0.1-SNAPSHOT1'
        }
    }
}
```

Full demos are in [`/examples`](./examples).
