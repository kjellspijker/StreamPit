package tk.sliomere.streampit.dialog

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.R

class DialogViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    var iconImageButton: ImageButton = view.findViewById(R.id.dialog_card_image_button)

    fun bindViewHolder(context: Context, dialogCard: DialogCard) {
        iconImageButton.setImageDrawable(context.resources.getDrawable(context.resources.getIdentifier(dialogCard.icon, "drawable", "tk.sliomere.streampit"), context.theme))
        iconImageButton.setBackgroundColor(dialogCard.color)

        iconImageButton.setOnClickListener{v: View? ->
            val intent = Intent(DialogFragment.eventCloseDialog)
            intent.putExtra(DialogFragment.extraIconName, dialogCard.icon)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

}