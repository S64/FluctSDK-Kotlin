package jp.s64.kotlin.fluctsdk

expect object Fluct {
    val version: String
}

expect class PlatformContext

enum class PlatformType {
    IOS, ANDROID
}
