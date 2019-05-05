package jp.s64.kotlin.example.myfluctapp.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import jp.s64.kotlin.fluctsdk.rewardedvideo.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val showButton by lazy { findViewById<Button>(R.id.show) }
    private val logList by lazy { findViewById<MyLoggerView>(R.id.log) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showButton
            .setOnClickListener {
                onClickShow()
            }

        finalizeSession()
    }

    private fun onClickShow() {
        showButton.isEnabled = false

        val rv = RewardedVideo.getInstance(
            groupId = MyConsts.groupId,
            unitId = MyConsts.unitId,
            context = this@MainActivity
        )

        indicate("Loading...")

        rv.load {
            when (it) {
                is Success -> {
                    indicate("onLoaded")
                    onLoaded(it.value)
                }
                is Failure -> {
                    indicate(it.reason.toString())
                    finalizeSession()
                }
            }
        }
    }

    private fun onLoaded(viewable: ViewableRewardedVideo) {
        indicate("Showing...")

        viewable.show {
            when (it) {
                is Success -> when (it.value) {
                    Visibility.OPENED -> indicate("onOpened")
                    Visibility.SHOULD_REWARD -> indicate("onShouldReward")
                    Visibility.CLOSED -> {
                        indicate("onClosed")
                        finalizeSession()
                    }
                }
                is Failure -> {
                    indicate(it.reason.toString())
                    finalizeSession()
                }
            }
        }

    }

    private fun finalizeSession() {
        showButton.isEnabled = true
    }

    private fun indicate(msg: String) {
        Log.d(TAG, msg)
        logList.log(msg)
    }

}
