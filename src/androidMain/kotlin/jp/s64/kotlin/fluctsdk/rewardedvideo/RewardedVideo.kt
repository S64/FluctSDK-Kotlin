package jp.s64.kotlin.fluctsdk.rewardedvideo

import jp.s64.kotlin.fluctsdk.PlatformContext

actual data class RewardedVideo(
    private val rv: jp.fluct.fluctsdk.FluctRewardedVideo,
    private val groupId: GroupId,
    private val unitId: UnitId
) {

    actual companion object {

        private val globalListener = object : jp.fluct.fluctsdk.FluctRewardedVideo.Listener {

            override fun onOpened(groupId: GroupId, unitId: UnitId) {
                localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                        .forEach {
                            it.onOpened()
                        }
            }

            override fun onFailedToPlay(groupId: GroupId, unitId: UnitId, errorCode: jp.fluct.fluctsdk.FluctErrorCode) {
                localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                        .forEach {
                            it.onFailedToPlay(
                                    convertErrorCode(errorCode)
                            )
                        }
            }

            override fun onShouldReward(groupId: GroupId, unitId: UnitId) {
                localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                        .forEach {
                            it.onShouldReward()
                        }
            }

            override fun onStarted(p0: String?, p1: String?) {
                // no-op
            }

            override fun onClosed(p0: String?, p1: String?) {
                // no-op
            }

            override fun onLoaded(groupId: GroupId, unitId: UnitId) {
                localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                        .forEach {
                            it.onLoaded()
                        }
            }

            override fun onFailedToLoad(groupId: GroupId, unitId: UnitId, errorCode: jp.fluct.fluctsdk.FluctErrorCode) {
                localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                        .forEach {
                            it.onFailedToLoad(
                                    convertErrorCode(errorCode)
                            )
                        }
            }

        }

        val localListeners = mutableMapOf<UnitPair, MutableSet<LocalListener>>()

        actual fun getInstance(
            groupId: GroupId,
            unitId: UnitId,
            context: PlatformContext
        ): RewardedVideo {
            return RewardedVideo(
                rv = jp.fluct.fluctsdk.FluctRewardedVideo.getInstance(
                    groupId,
                    unitId,
                    context
                ).apply {
                    this@apply.setListener(globalListener)
                },
                groupId = groupId,
                unitId = unitId
            )
        }

    }

    private val unitPair = UnitPair(
        unitId = unitId,
        groupId = groupId
    )

    actual fun load(block: (Result<ViewableRewardedVideo, FluctErrorException>) -> Unit) {
        localListeners.getOrPut(unitPair, { mutableSetOf() })
                .add(
                        object : LocalListener {

                            override fun onLoaded() {
                                finalize(null)
                            }

                            override fun onFailedToLoad(errorCode: ErrorCode) {
                                finalize(errorCode)
                            }

                            private fun finalize(result: ErrorCode?) {
                                localListeners.get(unitPair)!!
                                        .remove(this)
                                if (result == null) {
                                    block(Success(
                                            ViewableRewardedVideo(
                                                    rv = rv,
                                                    unitId = unitId,
                                                    groupId = groupId
                                            )
                                    ))
                                } else {
                                    block(Failure(
                                            FluctErrorException(result)
                                    ))
                                }
                            }

                        }
                )

        rv.loadAd()
    }

    interface LocalListener {

        fun onOpened() { /* no-op */ }

        fun onLoaded() { /* no-op */ }

        fun onShouldReward() { /* no-op */ }

        fun onClosed() { /* no-op */ }

        fun onFailedToLoad(errorCode: ErrorCode) { /* no-op */ }

        fun onFailedToPlay(errorCode: ErrorCode) { /* no-op */ }

    }

}

actual data class ViewableRewardedVideo(
    private val rv: jp.fluct.fluctsdk.FluctRewardedVideo,
    private val groupId: GroupId,
    private val unitId: UnitId

) {

    private val unitPair = UnitPair(
            groupId = groupId,
            unitId = unitId
    )

    actual fun show(block: (Result<Visibility, FluctErrorException>) -> Unit) {
        RewardedVideo.localListeners.get(unitPair)!!
                .add(
                        object : RewardedVideo.LocalListener {

                            override fun onOpened() {
                                block(
                                        Success(Visibility.OPENED)
                                )
                            }

                            override fun onShouldReward() {
                                block(
                                        Success(Visibility.SHOULD_REWARD)
                                )
                            }

                            override fun onClosed() {
                                block(
                                        Success(Visibility.CLOSED)
                                )
                                finalize()
                            }

                            override fun onFailedToPlay(errorCode: ErrorCode) {
                                block(
                                        Failure(FluctErrorException(errorCode))
                                )
                                finalize()
                            }

                            private fun finalize() {
                                RewardedVideo.localListeners.get(unitPair)!!
                                        .remove(this)
                            }

                        }
                )

        rv.show()
    }

}

private fun convertErrorCode(
        org: jp.fluct.fluctsdk.FluctErrorCode
): ErrorCode = when (org) {
    jp.fluct.fluctsdk.FluctErrorCode.NOT_READY -> ErrorCode.NOT_READY
    jp.fluct.fluctsdk.FluctErrorCode.BAD_REQUEST -> ErrorCode.BAD_REQUEST
    jp.fluct.fluctsdk.FluctErrorCode.CONNECTION_TIMEOUT -> ErrorCode.CONNECTION_TIMEOUT
    jp.fluct.fluctsdk.FluctErrorCode.NO_ADS -> ErrorCode.NO_ADS
    jp.fluct.fluctsdk.FluctErrorCode.LOAD_FAILED -> ErrorCode.LOAD_FAILED
    jp.fluct.fluctsdk.FluctErrorCode.WRONG_CONFIGURATION -> ErrorCode.WRONG_CONFIGURATION
    jp.fluct.fluctsdk.FluctErrorCode.NOT_CONNECTED_TO_INTERNET -> ErrorCode.NOT_CONNECTED_TO_INTERNET
    jp.fluct.fluctsdk.FluctErrorCode.VIDEO_PLAY_FAILED -> ErrorCode.VIDEO_PLAY_FAILED
    jp.fluct.fluctsdk.FluctErrorCode.EXPIRED -> ErrorCode.EXPIRED
    jp.fluct.fluctsdk.FluctErrorCode.PLAY_SERVICES_UNAVAILABLE -> ErrorCode.PLAY_SERVICES_UNAVAILABLE
    jp.fluct.fluctsdk.FluctErrorCode.ILLEGAL_STATE -> ErrorCode.ILLEGAL_STATE
    jp.fluct.fluctsdk.FluctErrorCode.UNKNOWN -> ErrorCode.UNKNOWN
}
