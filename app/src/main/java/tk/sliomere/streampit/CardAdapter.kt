package tk.sliomere.streampit

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(var context: Context, var idList: ArrayList<Int>, var cardList: HashMap<Int, Card>) : RecyclerView.Adapter<CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card, parent, false)
        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card: Card = cardList[idList[position]]!!
        holder.title.text = card.name
        holder.card = card
        holder.bindViewHolder()
    }

    override fun getItemCount(): Int {
        return cardList.size
    }
}