package tk.sliomere.streampit.dialog

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.R

class ChooseTargetDialogViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    var dialogCardView: CardView = view.findViewById(R.id.choose_target_card_view)
    var targetNameTextView: TextView = view.findViewById(R.id.target_name_text_view)

    fun bindViewHolder(context: Context, dialogCard: ChooseTargetDialogCard) {
        dialogCardView.setOnClickListener{v: View? ->
            val intent = Intent(ChooseTargetDialogFragment.eventCloseDialog)
            intent.putExtra(ChooseTargetDialogFragment.extraTargetName, dialogCard.targetName)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        targetNameTextView.text = dialogCard.targetName

    }

}