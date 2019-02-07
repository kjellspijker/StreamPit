package tk.sliomere.streampit.cards

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import tk.sliomere.streampit.CardAction
import tk.sliomere.streampit.MainActivity

class ToggleVisibilityCard(id: String, name: String, color: Int, icon: String, target: String) : Card(id, name, color, icon, CardAction.TOGGLE_VISIBILITY, target) {

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), jsonObject.getString("target"))

    override fun reloadCard() {

    }

    override fun onClickListener(context: Context): Boolean {
        if (!super.onClickListener(context)) {
            Log.d("StreamPit", "Toggle Visibility")
            val args = JSONObject()
            args.put("item", target)
            args.put("visible", false)
            MainActivity.webSocketClient.sendMessage("SetSceneItemProperties", args)
            MainActivity.webSocketClient.sendMessage("GetCurrentScene", JSONObject(), callback = { msg: JSONObject ->
                val args = JSONObject()
                args.put("scene-name", msg.getString("name"))
                MainActivity.webSocketClient.sendMessage("SetCurrentScene", args)
            })
        }
        return true
    }
}