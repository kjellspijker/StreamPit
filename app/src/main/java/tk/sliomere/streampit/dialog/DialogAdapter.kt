package tk.sliomere.streampit.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.R

class DialogAdapter(var context: Context, var dialogCardList: ArrayList<DialogCard>) : RecyclerView.Adapter<DialogViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.dialog_card, parent, false)
        return DialogViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dialogCardList.size
    }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        val dialogCard: DialogCard = dialogCardList[position]
        holder.bindViewHolder(context, dialogCard)
    }
}