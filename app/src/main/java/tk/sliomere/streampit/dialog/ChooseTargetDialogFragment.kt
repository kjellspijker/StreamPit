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

class ChooseTargetDialogFragment : DialogFragment() {

    companion object {
        const val eventCloseDialog = "CLOSETARGETDIALOGEVENT"
        const val extraTargetName = "TARGETNAME"
    }

    val dialogCardList: ArrayList<ChooseTargetDialogCard> = ArrayList()
    private lateinit var decor: RecyclerView.ItemDecoration
    private var fragment = this

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_choose_target, container, false)

        LocalBroadcastManager.getInstance(v.context).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val int = Intent(NewCardActivity.eventChooseTargetDialogClosed)
                int.putExtra(extraTargetName, intent!!.getStringExtra(extraTargetName))
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(int)
                dismiss()
            }
        }, IntentFilter(eventCloseDialog))

        dialogCardList.clear()
        val targetNames = this.arguments!!.getStringArrayList("targets")!!
        for (targetName in targetNames) {
            dialogCardList.add(ChooseTargetDialogCard(targetName))
        }

        decor = GridSpacingItemDecoration(1, dpToPx(10), true)

        val adapter = ChooseTargetDialogAdapter(context!!, dialogCardList)
        val recyclerView: RecyclerView = v.findViewById(R.id.choose_target_recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(decor)
        recyclerView.layoutManager = GridLayoutManager(context, 1)
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