package tk.sliomere.streampit

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.cards.Card

class CardViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

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

    fun bindViewHolder(card: Card) {
        this.card = card
        this.card.vh = this
        title.setBackgroundColor(card.color)
        icon.setBackgroundColor(card.color)
        icon.setImageDrawable(view.context.resources.getDrawable(view.context.resources.getIdentifier(card.icon, "drawable", "tk.sliomere.streampit"), view.context.theme))
    }

    fun toggleMute() {
        var colorFilter: Int = view.resources.getColor(android.R.color.white, view.context.theme)
        if (card.icon == "icon_volume") {
            card.icon = "icon_mute"
            colorFilter = view.resources.getColor(R.color.colorMuted, view.context.theme)
        } else if (card.icon == "icon_mute") {
            card.icon = "icon_volume"
        }
        icon.setImageDrawable(view.context.resources.getDrawable(view.context.resources.getIdentifier(card.icon, "drawable", "tk.sliomere.streampit"), view.context.theme))
        icon.setColorFilter(colorFilter)
    }

}
