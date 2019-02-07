package tk.sliomere.streampit.cards

import android.graphics.Color
import org.json.JSONObject
import tk.sliomere.streampit.CardAction

class NothingCard(id: String, name: String, color: Int, icon: String, target: String) : Card(id, name, color, icon, CardAction.NOTHING, target) {
    override fun reloadCard() {

    }

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), jsonObject.getString("target"))
}