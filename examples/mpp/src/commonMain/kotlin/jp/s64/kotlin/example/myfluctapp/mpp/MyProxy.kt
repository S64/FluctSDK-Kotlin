package jp.s64.kotlin.example.myfluctapp.mpp

import jp.s64.kotlin.fluctsdk.PlatformContext
import jp.s64.kotlin.fluctsdk.rewardedvideo.Failure
import jp.s64.kotlin.fluctsdk.rewardedvideo.RewardedVideo
import jp.s64.kotlin.fluctsdk.rewardedvideo.Success
import jp.s64.kotlin.fluctsdk.rewardedvideo.Visibility

class MyProxy(
        listener: MyProxyDelegate
): MyProxyDelegate by listener {

    fun loadAndShowRv(context: PlatformContext) {
        val rv = RewardedVideo.getInstance(
                MyGroupId,
                MyUnitId,
                context
        )

        onEvent("Loading...")

        rv.load {
            when (it) {
                is Success -> {
                    onEvent("onLoaded")
                    onEvent("Showing...")
                    it.value.show {
                        when (it) {
                            is Success -> when (it.value) {
                                Visibility.OPENED -> onEvent("onOpened")
                                Visibility.CLOSED -> {
                                    onEvent("onClosed")
                                    onFinish()
                                }
                                Visibility.SHOULD_REWARD -> onEvent("onShouldReward")
                            }
                            is Failure ->{
                                onError(it.reason.toString())
                                onFinish()
                            }
                        }
                    }
                }
                is Failure -> {
                    onError(it.reason.toString())
                    onFinish()
                }
            }
        }
    }

}

interface MyProxyDelegate {

    fun onEvent(msg: String)

    fun onError(msg: String)

    fun onFinish()

}
