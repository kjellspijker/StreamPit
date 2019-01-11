package tk.sliomere.streampit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject

class Card(val id: String, var name: String, var color: Int, var icon: String) : Parcelable {
    constructor(id: String, jsonObject: JSONObject) : this(id, jsonObject.getString("name")!!, Color.parseColor(jsonObject.getString("color")!!), jsonObject.getString("icon"))

    fun onClickListener(context: Context) {
        Log.d("StreamPit", "Click Listener")
        if (MainActivity.removingCard) {
            val intent = Intent(MainActivity.eventRemoveCard)
            intent.putExtra(MainActivity.cardIDExtra, id)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    fun onLongClickListener(context: Context) {
        Log.d("StreamPit", "Long Click Listener")
    }

    fun toJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("icon", icon)
        json.put("color", "#" + Integer.toHexString(color))
        return json
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readString()!!, parcel.readInt(), parcel.readString()!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeInt(color)
        dest.writeString(icon)
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
