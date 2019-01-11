package tk.sliomere.streampit.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tk.sliomere.streampit.GridSpacingItemDecoration
import tk.sliomere.streampit.NewCardActivity
import tk.sliomere.streampit.R

class DialogFragment : DialogFragment() {

    companion object {
        const val eventCloseDialog = "CLOSEDIALOGEVENT"
        const val extraIconName = "ICONNAME"
    }

    val dialogCardList: ArrayList<DialogCard> = ArrayList()
    private lateinit var decor: RecyclerView.ItemDecoration
    private var fragment = this

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_choose_icon, container, false)

        LocalBroadcastManager.getInstance(v.context).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val int = Intent(NewCardActivity.eventDialogClosed)
                int.putExtra(extraIconName, intent!!.getStringExtra(extraIconName))
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(int)
                dismiss()
            }
        }, IntentFilter(eventCloseDialog))

        dialogCardList.clear()
        val availableIcons = NewCardActivity.availableIcons
        for (icon in availableIcons) {
            dialogCardList.add(DialogCard(NewCardActivity.color, icon))
        }

        decor = GridSpacingItemDecoration(2, dpToPx(10), true)

        val adapter = DialogAdapter(context!!, dialogCardList)
        val recyclerView: RecyclerView = v.findViewById(R.id.dialog_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(decor)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.itemAnimator = DefaultItemAnimator()

        return v
    }

    /**
     * Calculate the amount of pixels from dp
     */
    private fun dpToPx(dp: Int): Int {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics))
    }

}