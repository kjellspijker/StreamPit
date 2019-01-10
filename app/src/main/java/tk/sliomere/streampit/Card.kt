package tk.sliomere.streampit

import android.graphics.Color
import android.util.Log
import org.json.JSONObject

class Card (var name: String, var color: Int) {
    constructor(jsonObject: JSONObject) : this(jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!))

    fun onClickListener() {
        Log.d("StreamPit", "Click Listener")
    }

    fun onLongClickListener() {
        Log.d("StreamPit", "Long Click Listener")
    }

    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("color", Integer.toHexString(color))
        return json
    }

}