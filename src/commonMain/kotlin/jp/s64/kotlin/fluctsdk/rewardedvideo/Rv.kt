package jp.s64.kotlin.fluctsdk.rewardedvideo

import jp.s64.kotlin.fluctsdk.PlatformContext
import jp.s64.kotlin.fluctsdk.PlatformType

typealias GroupId = String
typealias UnitId = String

data class UnitPair(
    val groupId: GroupId,
    val unitId: UnitId
)

sealed class Result<V, E>

data class Success<V, E>(val value: V): Result<V, E>()
data class Failure<V, E>(val reason: E): Result<V, E>()

expect class RewardedVideo {

    companion object {

        fun getInstance(
            groupId: GroupId,
            unitId: UnitId,
            context: PlatformContext
        ): RewardedVideo

    }

    fun load(
        block: (Result<ViewableRewardedVideo, FluctErrorException>) -> Unit
    )

}

expect class ViewableRewardedVideo {

    fun show(
        block: (Result<Visibility, FluctErrorException>) -> Unit
    )

}

enum class ErrorCode(
        val platforms: Set<PlatformType>
) {
    NOT_READY(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    BAD_REQUEST(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    CONNECTION_TIMEOUT(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    NO_ADS(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    LOAD_FAILED(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    WRONG_CONFIGURATION(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    NOT_CONNECTED_TO_INTERNET(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    VIDEO_PLAY_FAILED(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    EXPIRED(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    PLAY_SERVICES_UNAVAILABLE(setOf(PlatformType.ANDROID)),
    ILLEGAL_STATE(setOf(PlatformType.ANDROID)),
    UNKNOWN(setOf(PlatformType.IOS, PlatformType.ANDROID)),
    INITIALIZE_FAILED(setOf(PlatformType.IOS)),
    VAST_PARSE_FAILED(setOf(PlatformType.IOS)),
}

data class FluctErrorException(
    val errorCode: ErrorCode
) : Throwable()

enum class Visibility {
    OPENED,
    SHOULD_REWARD,
    CLOSED,
}
