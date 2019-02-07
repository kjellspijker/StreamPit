package tk.sliomere.streampit.dialog

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.R

class DialogViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    var dialogCardView: CardView = view.findViewById(R.id.dialog_card_view)
    var iconImageButton: ImageButton = view.findViewById(R.id.dialog_card_image_button)
    var actionNameTextView: TextView = view.findViewById(R.id.action_name_text_view)

    fun bindViewHolder(context: Context, dialogCard: DialogCard) {
        iconImageButton.setImageDrawable(context.resources.getDrawable(context.resources.getIdentifier(dialogCard.icon, "drawable", "tk.sliomere.streampit"), context.theme))
        iconImageButton.setBackgroundColor(dialogCard.color)

        dialogCardView.setOnClickListener{v: View? ->
            val intent = Intent(DialogFragment.eventCloseDialog)
            intent.putExtra(DialogFragment.extraIconName, dialogCard.icon)
            intent.putExtra(DialogFragment.extraCardAction, dialogCard.action)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        val text: String = when (dialogCard.icon) {
            "icon_scene" -> { "Change Scene" }
            "icon_volume" -> { "Toggle Audio/Mic" }
            "icon_play" -> { "Toggle Streaming" }
            "icon_record_rec" -> { "Toggle Recording" }
            "icon_visible" -> { "Toggle Visibility" }
            else -> { "" }
        }

        actionNameTextView.text = text

    }

}