package jp.s64.kotlin.fluctsdk

import platform.UIKit.UIViewController

actual object Fluct {

    actual val version = jp.fluct.fluctsdk.FluctSDK.version()

}

actual typealias PlatformContext = UIViewController
