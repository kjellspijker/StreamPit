package tk.sliomere.streampit.cards

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject
import tk.sliomere.streampit.CardAction
import tk.sliomere.streampit.CardViewHolder
import tk.sliomere.streampit.MainActivity
import tk.sliomere.streampit.R

abstract class Card(val id: String, var name: String, var color: Int, var icon: String, var cardAction: CardAction, var target: String) : Parcelable {

    lateinit var vh: CardViewHolder

    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"), CardAction.valueOf(jsonObject.getString("cardAction")), jsonObject.getString("target"))

    open fun onClickListener(context: Context): Boolean {
        if (MainActivity.removingCard) {
            val intent = Intent(MainActivity.eventRemoveCard)
            intent.putExtra(MainActivity.cardIDExtra, id)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            return true
        }
        return false
    }

    open fun onLongClickListener(context: Context) {
        reloadCard()
        Toast.makeText(vh.view.context, vh.view.context.resources.getString(R.string.card_status_updated), Toast.LENGTH_SHORT).show()
    }

    open fun toJSON(): JSONObject {
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
    abstract fun reloadCard()

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            val id: String = parcel.readString()!!
            val name: String = parcel.readString()!!
            val color: Int = parcel.readInt()
            val icon: String = parcel.readString()!!
            val cardAction: CardAction = CardAction.valueOf(parcel.readString()!!)
            val target: String = parcel.readString()!!
            return when (cardAction) {
                CardAction.TOGGLE_MUTE -> ToggleMuteCard(id, name, color, icon, target)
                CardAction.TOGGLE_RECORDING -> ToggleRecordingCard(id, name, color, icon, target)
                CardAction.SWITCH_SCENE -> SwitchSceneCard(id, name, color, icon, target)
                CardAction.TOGGLE_STREAMING -> ToggleStreamingCard(id, name, color, icon, target)
                CardAction.TOGGLE_VISIBILITY -> ToggleVisibilityCard(id, name, color, icon, target)
                CardAction.NOTHING -> NothingCard(id, name, color, icon, target)
            }
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }

}
