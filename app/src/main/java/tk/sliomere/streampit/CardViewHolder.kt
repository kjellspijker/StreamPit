package tk.sliomere.streampit

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    var title: TextView = view.findViewById(R.id.card_title)
    var icon: ImageView = view.findViewById(R.id.card_image_view)
    private var cardView: CardView = view.findViewById(R.id.card_card_view)
    lateinit var card: Card

    init {
        cardView.setOnClickListener { v ->
            card.onClickListener(v.context)
        }

        cardView.setOnLongClickListener { v ->
            card.onLongClickListener(v.context)
            true
        }
    }

    fun bindViewHolder() {
        title.setBackgroundColor(card.color)
        icon.setBackgroundColor(card.color)
    }

}
