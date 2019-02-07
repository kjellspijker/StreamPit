package tk.sliomere.streampit

enum class CardAction {

    NOTHING, TOGGLE_MUTE, SWITCH_SCENE, TOGGLE_STREAMING, TOGGLE_RECORDING, TOGGLE_VISIBILITY;

    companion object {
        fun parse(string: String) : CardAction {
            return when (string) {
                "icon_volume" -> TOGGLE_MUTE
                "icon_play" -> TOGGLE_STREAMING
                "icon_record_rec" -> TOGGLE_RECORDING
                "icon_scene" -> SWITCH_SCENE
                "icon_visible" -> TOGGLE_VISIBILITY
                else -> NOTHING
            }
        }
    }

}