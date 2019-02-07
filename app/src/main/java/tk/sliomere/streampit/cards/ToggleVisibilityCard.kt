package tk.sliomere.streampit.cards

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import tk.sliomere.streampit.CardAction
import tk.sliomere.streampit.MainActivity

class ToggleVisibilityCard(id: String, name: String, color: Int, icon: String, target: String) : Card(id, name, color, icon, CardAction.TOGGLE_VISIBILITY, target) {

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), jsonObject.getString("target"))

    init {
        if (!MainActivity.listeningCards.containsKey(CardAction.TOGGLE_VISIBILITY)) {
            MainActivity.listeningCards[CardAction.TOGGLE_VISIBILITY] = ArrayList()
        }
        MainActivity.listeningCards[CardAction.TOGGLE_VISIBILITY]!!.add(this)
    }

    override fun reloadCard() {
        val args = JSONObject()
        args.put("item", target)
        MainActivity.webSocketClient.sendMessage("GetSceneItemProperties", args, callback = { msg: JSONObject ->
            vh.toggleVisibility(msg.getBoolean("visible"))
        })
    }

    override fun onClickListener(context: Context): Boolean {
        if (!super.onClickListener(context)) {
            Log.d("StreamPit", "Toggle Visibility")
            var args = JSONObject()
            args.put("item", target)
            MainActivity.webSocketClient.sendMessage("GetSceneItemProperties", args, callback = { msg: JSONObject ->
                args = JSONObject()
                args.put("item", target)
                args.put("visible", !msg.getBoolean("visible"))
                MainActivity.webSocketClient.sendMessage("SetSceneItemProperties", args)
                if (MainActivity.OBS_STUDIO_MODE_ENABLED) {
                    Log.d("StreamPit", "STUDIO_ENABLED, WEIRD SHIT")
                    MainActivity.webSocketClient.sendMessage("GetCurrentScene", JSONObject(), callback = { msg: JSONObject ->
                        args = JSONObject()
                        args.put("scene-name", msg.getString("name"))
                        MainActivity.webSocketClient.sendMessage("SetCurrentScene", args)
                    })
                } else {
                    Log.d("StreamPit", "STUDIO_DISABLED, NO WEIRD SHIT")
                }
            })
        }
        return true
    }

    fun onVisibilityUpdate(itemName: String, itemVisible: Boolean) {
        if (this.target == itemName) {
            vh.toggleVisibility(itemVisible)
        }
    }
}