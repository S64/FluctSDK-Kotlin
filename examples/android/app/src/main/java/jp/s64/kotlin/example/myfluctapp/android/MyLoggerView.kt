package jp.s64.kotlin.example.myfluctapp.android

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi

class MyLoggerView : ListView {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context?) : super(context) {}

    private val logAdapter: ArrayAdapter<String>

    init {
        logAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)
            .also {
                this.adapter = it
            }
    }

    fun log(msg: String) {
        logAdapter.insert(msg, 0)
    }

}
