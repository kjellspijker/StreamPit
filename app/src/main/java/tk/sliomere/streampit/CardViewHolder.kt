package tk.sliomere.streampit

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var title: TextView = view.findViewById(R.id.card_title)
    var icon: ImageView = view.findViewById(R.id.card_image_view)
    var cardView: CardView = view.findViewById(R.id.card_card_view)
    lateinit var card: Card

    init {
        cardView.setOnClickListener { v ->
            card.onClickListener()
        }

        cardView.setOnLongClickListener { v ->
            card.onLongClickListener()
            true
        }
    }

    fun bindViewHolder() {
        title.setBackgroundColor(card.color)
        icon.setBackgroundColor(card.color)
        cardView.setBackgroundColor(card.color)
    }

}
