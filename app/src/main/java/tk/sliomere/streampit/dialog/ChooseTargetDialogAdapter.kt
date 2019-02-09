package tk.sliomere.streampit.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.R

class ChooseTargetDialogAdapter(var context: Context, var dialogCardList: ArrayList<ChooseTargetDialogCard>) : RecyclerView.Adapter<ChooseTargetDialogViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseTargetDialogViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.dialog_choose_target_card, parent, false)
        return ChooseTargetDialogViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dialogCardList.size
    }

    override fun onBindViewHolder(holder: ChooseTargetDialogViewHolder, position: Int) {
        val dialogCard: ChooseTargetDialogCard = dialogCardList[position]
        holder.bindViewHolder(context, dialogCard)
    }
}