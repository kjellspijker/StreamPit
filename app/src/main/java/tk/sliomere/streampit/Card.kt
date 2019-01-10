package tk.sliomere.streampit

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.json.JSONObject

class Card (var name: String, var color: Int) : Parcelable {
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

    constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readInt()) {

    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeInt(color)
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
