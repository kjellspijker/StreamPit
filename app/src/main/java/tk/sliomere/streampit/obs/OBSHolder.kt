package tk.sliomere.streampit.obs

import org.json.JSONArray

class OBSHolder {

    var audioDevices: ArrayList<String> = ArrayList()

    fun parseSourcesList(sourcesList: JSONArray) {
        for (i in 0 until sourcesList.length()) {
            val source = sourcesList.getJSONObject(i)
            if (source.getString("typeId").contains("wasapi")) {
                audioDevices.add(source.getString("name"))
            }
        }
    }

}