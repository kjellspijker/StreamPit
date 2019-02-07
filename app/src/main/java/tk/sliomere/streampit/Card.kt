package tk.sliomere.streampit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject

class Card(val id: String, var name: String, var color: Int, var icon: String, var cardAction: CardAction, var target: String) : Parcelable {

    lateinit var vh: CardViewHolder

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), CardAction.valueOf(jsonObject.getString("cardAction")), jsonObject.getString("target"))

    fun onClickListener(context: Context) {
        Log.d("StreamPit", "Click Listener")
        if (MainActivity.removingCard) {
            val intent = Intent(MainActivity.eventRemoveCard)
            intent.putExtra(MainActivity.cardIDExtra, id)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        when (cardAction) {
            CardAction.TOGGLE_MUTE -> {
                val args = JSONObject()
                args.put("source", target)
                MainActivity.webSocketClient.sendMessage("ToggleMute",  args)

                vh.toggleMute()
            }
            CardAction.SWITCH_SCENE -> {
                Log.d("StreamPit", "Switch Scene")
                val args = JSONObject()
                args.put("scene-name", target)
                MainActivity.webSocketClient.sendMessage("SetCurrentScene", args)
            }
            CardAction.TOGGLE_STREAMING -> {
                Log.d("StreamPit", "Toggle Streaming")
            }
            CardAction.TOGGLE_RECORDING -> {
                Log.d("StreamPit", "Toggle Recording")
            }
            CardAction.TOGGLE_VISIBILITY -> {
                Log.d("StreamPit", "Toggle Visibility")
                val args = JSONObject()
                args.put("item", target)
                args.put("visible", false)
                MainActivity.webSocketClient.sendMessage("SetSceneItemProperties", args)
                MainActivity.webSocketClient.sendMessage("GetCurrentScene", JSONObject(), callback = {msg: JSONObject ->
                    val args = JSONObject()
                    args.put("scene-name", msg.getString("name"))
                    MainActivity.webSocketClient.sendMessage("SetCurrentScene", args)
                })
            }
            CardAction.NOTHING -> {}
            else -> {
                throw Exception("No action registered for CardAction " + this.cardAction)
            }
        }
    }

    fun onLongClickListener(context: Context) {
        Log.d("StreamPit", "Long Click Listener")
        if (cardAction == CardAction.TOGGLE_MUTE) {
            reloadCard()
        }
        Toast.makeText(vh.view.context, vh.view.context.resources.getString(R.string.card_status_updated), Toast.LENGTH_SHORT).show()
    }

    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("cardAction", cardAction.name)
        json.put("icon", icon)
        json.put("color", "#" + Integer.toHexString(color))
        json.put("target", target)
        return json
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readString()!!, parcel.readInt(), parcel.readString()!!, CardAction.valueOf(parcel.readString()!!), parcel.readString()!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeInt(color)
        dest.writeString(icon)
        dest.writeString(cardAction.name)
        dest.writeString(target)
    }

    /**
     * Reload the settings of this card after a possible change in OBS
     * Example: muting desktop audio manually on obs will not cause it to update in StreamPit,
     *          this function reloads the card to the correct state
     */
    fun reloadCard() {
        if (this.cardAction == CardAction.TOGGLE_MUTE) {
            val args1 = JSONObject()
            args1.put("source", "Desktop Audio")
            MainActivity.webSocketClient.sendMessage("GetMute", args1, callback = { msg: JSONObject ->
                val color: Int
                icon = if (msg.getBoolean("muted")) {
                    color = vh.view.resources.getColor(R.color.colorMuted, vh.view.context.theme)
                    "icon_mute"
                } else {
                    color = vh.view.resources.getColor(android.R.color.white, vh.view.context.theme)
                    "icon_volume"
                }
                vh.icon.setImageDrawable(vh.view.context.resources.getDrawable(vh.view.context.resources.getIdentifier(icon, "drawable", "tk.sliomere.streampit"), vh.view.context.theme))
                vh.icon.setColorFilter(color)
            })
        }
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }

}
