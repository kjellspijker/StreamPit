package tk.sliomere.streampit.cards

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import tk.sliomere.streampit.CardAction

class ToggleRecordingCard(id: String, name: String, color: Int, icon: String, target: String) : Card(id, name, color, icon, CardAction.TOGGLE_RECORDING, target) {

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), jsonObject.getString("target"))

    override fun reloadCard() {

    }

    override fun onClickListener(context: Context): Boolean {
        if (!super.onClickListener(context)) {
            Log.d("StreamPit", "Toggle Recording")
        }
        return true
    }

}