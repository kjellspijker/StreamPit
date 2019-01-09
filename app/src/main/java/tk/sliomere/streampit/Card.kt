package tk.sliomere.streampit

import android.util.Log

class Card (var name: String, var color: Int) {

    fun onClickListener() {
        Log.d("StreamPit", "Click Listener")
    }

    fun onLongClickListener() {
        Log.d("StreamPit", "Long Click Listener")
    }

}