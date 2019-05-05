package jp.s64.kotlin.fluctsdk

import android.app.Activity

actual typealias PlatformContext = Activity

actual object Fluct {

    actual val version = jp.fluct.fluctsdk.Fluct.getVersion()

}
