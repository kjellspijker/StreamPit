package tk.sliomere.streampit.cards

import android.content.Context
import android.graphics.Color
import org.json.JSONObject
import tk.sliomere.streampit.CardAction
import tk.sliomere.streampit.MainActivity
import tk.sliomere.streampit.R

class SwitchSceneCard(id: String, name: String, color: Int, icon: String, target: String) : Card(id, name, color, icon, CardAction.SWITCH_SCENE, target) {

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), jsonObject.getString("target"))

    init {
        if (!MainActivity.listeningCards.containsKey(CardAction.SWITCH_SCENE)) {
            MainActivity.listeningCards[CardAction.SWITCH_SCENE] = ArrayList()
        }
        MainActivity.listeningCards[CardAction.SWITCH_SCENE]!!.add(this)
    }

    override fun onClickListener(context: Context): Boolean {
        if (!super.onClickListener(context)) {
            val args = JSONObject()
            args.put("scene-name", target)
            MainActivity.webSocketClient.sendMessage("SetCurrentScene", args)
            vh.icon.setColorFilter(vh.view.resources.getColor(R.color.colorCurrentScene, vh.view.context.theme))
        }
        return true
    }

    private var bindCallbackMsg: JSONObject? = null

    override fun bindCompleted() {
        if (bindCallbackMsg != null) {
            doReload(bindCallbackMsg!!)
            bindCallbackMsg = null
        }
    }

    private fun doReload(msg: JSONObject) {
        val cl = if (this.target == msg.getString("name")) {
            vh.view.resources.getColor(R.color.colorCurrentScene, vh.view.context.theme)
        } else {
            vh.view.resources.getColor(android.R.color.white, vh.view.context.theme)
        }
        vh.icon.setColorFilter(cl)
    }

    override fun reloadCard() {
        //TODO make this unnecessary
        MainActivity.webSocketClient.sendMessage("GetCurrentScene", JSONObject(), callback = { msg: JSONObject ->
            try {
                doReload(msg)
            } catch (e: Exception) {
                this.bindCallbackMsg = msg
            }
        })
    }

    fun onSceneUpdate(name: String) {
        try {
            if (this.target == name) {
                vh.icon.setColorFilter(vh.view.resources.getColor(R.color.colorCurrentScene, vh.view.context.theme))
            } else {
                vh.icon.setColorFilter(vh.view.resources.getColor(android.R.color.white, vh.view.context.theme))
            }
        } catch (e: Exception) {

        }
    }
}