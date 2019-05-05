package jp.s64.kotlin.fluctsdk.rewardedvideo

import co.touchlab.stately.collections.frozenHashMap
import co.touchlab.stately.collections.frozenHashSet
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import jp.fluct.fluctsdk.FSSRewardedVideo
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorBadRequest
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorExpired
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorInitializeFailed
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorLoadFailed
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorNoAds
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorNotConnectedToInternet
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorNotReady
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorPlayFailed
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorTimeout
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorUnknown
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorVastParseFailed
import jp.fluct.fluctsdk.FSSRewardedVideoAdErrorWrongConfiguration
import jp.fluct.fluctsdk.FSSRewardedVideoDelegateProtocol
import jp.s64.kotlin.fluctsdk.PlatformContext
import platform.Foundation.NSError
import platform.darwin.NSObject

actual data class RewardedVideo(
        private val rv: FSSRewardedVideo,
        private val groupId: GroupId,
        private val unitId: UnitId,
        private val context: PlatformContext
) {

    actual companion object {

        @ThreadLocal
        internal val lock = Lock()

        actual fun getInstance(
            groupId: GroupId,
            unitId: UnitId,
            context: PlatformContext
        ): RewardedVideo {
            return RewardedVideo(
                    rv = FSSRewardedVideo.sharedInstance
                            .apply {
                                this.delegate = globalDelegate
                            },
                    groupId = groupId,
                    unitId = unitId,
                    context = context
            )
        }

        @ThreadLocal
        private val globalDelegate = object : NSObject(), FSSRewardedVideoDelegateProtocol {

            override fun rewardedVideoDidAppearForGroupId(groupId: GroupId, unitId: UnitId) {
                lock.withLock {
                    localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                            .forEach {
                                it.didAppear()
                            }
                }
            }

            override fun rewardedVideoDidDisappearForGroupId(groupId: GroupId, unitId: UnitId) {
                lock.withLock {
                    localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                            .forEach {
                                it.didDisappear()
                            }
                }
            }

            override fun rewardedVideoDidFailToLoadForGroupId(groupId: GroupId, unitId: UnitId, error: NSError) {
                lock.withLock {
                    localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                            .forEach {
                                it.didFailToLoad(error)
                            }
                }
            }

            override fun rewardedVideoDidFailToPlayForGroupId(groupId: GroupId, unitId: UnitId, error: NSError) {
                lock.withLock {
                    localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                            .forEach {
                                it.didFailToPlay(error)
                            }
                }
            }

            override fun rewardedVideoDidLoadForGroupID(groupId: GroupId, unitId: UnitId) {
                lock.withLock {
                    localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                            .forEach {
                                it.didLoad()
                            }
                }
            }

            override fun rewardedVideoShouldRewardForGroupID(groupId: GroupId, unitId: UnitId) {
                lock.withLock {
                    localListeners.get(UnitPair(groupId = groupId, unitId = unitId))!!
                            .forEach {
                                it.shouldReward()
                            }
                }
            }

            override fun rewardedVideoWillAppearForGroupId(groupId: GroupId, unitId: UnitId) {
                // no-op
            }

            override fun rewardedVideoWillDisappearForGroupId(groupId: GroupId, unitId: UnitId) {
                // no-op
            }
        }

        @ThreadLocal
        val localListeners = frozenHashMap<UnitPair, MutableSet<LocalListener>>()

    }

    @ThreadLocal
    private val unitPair = UnitPair(
            groupId = groupId,
            unitId = unitId
    )

    actual fun load(block: (Result<ViewableRewardedVideo, FluctErrorException>) -> Unit) {
        val listener = object : LocalListener {

            override fun didFailToLoad(err: NSError) {
                finalize(err)
            }

            override fun didLoad() {
                finalize(null)
            }

            private fun finalize(err: NSError?) {
                lock.withLock {
                    localListeners.get(unitPair)!!
                            .remove(this)
                }

                if (err == null) {
                    block(Success(
                            ViewableRewardedVideo(
                                    rv = rv,
                                    groupId = groupId,
                                    unitId = unitId,
                                    context = context
                            )
                    ))
                } else {
                    block(Failure(
                            FluctErrorException(convertFromNSError(err))
                    ))
                }
            }

        }

        lock.withLock {
            val setRef = localListeners.getOrElse(unitPair, { frozenHashSet() })
            setRef.add(listener)
            localListeners.put(unitPair, setRef)
        }

        rv.loadRewardedVideoWithGroupId(groupId = groupId, unitId = unitId)
    }

    interface LocalListener {

        fun didAppear() { /* no-op */ }

        fun didDisappear() { /* no-op */ }

        fun didFailToLoad(err: NSError) { /* no-op */ }

        fun didFailToPlay(err: NSError) { /* no-op */ }

        fun shouldReward() { /* no-op */ }

        fun didLoad() { /* no-op */ }

    }

}

actual data class ViewableRewardedVideo(
        private val rv: FSSRewardedVideo,
        private val groupId: GroupId,
        private val unitId: UnitId,
        private val context: PlatformContext

) {

    @ThreadLocal
    private val unitPair = UnitPair(
            groupId = groupId,
            unitId = unitId
    )

    actual fun show(block: (Result<Visibility, FluctErrorException>) -> Unit) {
        val listener = object : RewardedVideo.LocalListener {

            override fun didFailToPlay(err: NSError) {
                block(
                        Failure(FluctErrorException(convertFromNSError(err)))
                )
                finalize()
            }

            override fun didAppear() {
                block(Success(Visibility.OPENED))
            }

            override fun didDisappear() {
                block(Success(Visibility.CLOSED))
                finalize()
            }

            override fun shouldReward() {
                block(Success(Visibility.SHOULD_REWARD))
            }

            private fun finalize() {
                RewardedVideo.lock.withLock {
                    RewardedVideo.localListeners.get(unitPair)!!
                            .remove(this)
                }
            }

        }

        RewardedVideo.lock.withLock {
            RewardedVideo.localListeners.get(unitPair)!!
                    .add(listener)
        }

        rv.presentRewardedVideoAdForGroupId(groupId, unitId, context)
    }

}

private fun convertFromNSError(err: NSError) = when (err.code) {
    FSSRewardedVideoAdErrorBadRequest -> ErrorCode.BAD_REQUEST
    FSSRewardedVideoAdErrorExpired -> ErrorCode.EXPIRED
    FSSRewardedVideoAdErrorInitializeFailed -> ErrorCode.INITIALIZE_FAILED
    FSSRewardedVideoAdErrorNoAds -> ErrorCode.NO_ADS
    FSSRewardedVideoAdErrorNotConnectedToInternet -> ErrorCode.NOT_CONNECTED_TO_INTERNET
    FSSRewardedVideoAdErrorNotReady -> ErrorCode.NOT_READY
    FSSRewardedVideoAdErrorPlayFailed -> ErrorCode.VIDEO_PLAY_FAILED
    FSSRewardedVideoAdErrorTimeout -> ErrorCode.CONNECTION_TIMEOUT
    FSSRewardedVideoAdErrorUnknown -> ErrorCode.UNKNOWN
    FSSRewardedVideoAdErrorVastParseFailed -> ErrorCode.VAST_PARSE_FAILED
    FSSRewardedVideoAdErrorWrongConfiguration -> ErrorCode.WRONG_CONFIGURATION
    FSSRewardedVideoAdErrorLoadFailed -> ErrorCode.LOAD_FAILED
    else -> TODO()
}
