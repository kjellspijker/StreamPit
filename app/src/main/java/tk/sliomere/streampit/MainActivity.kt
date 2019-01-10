package tk.sliomere.streampit

import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.util.TypedValue
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {
        const val cardExtra = "CARDEXTRA"
        const val eventDataSetChanged = "NOTIFYDATASETCHANGED"
    }

    private lateinit var cardList: ArrayList<Card>
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val PREF_NAME = "STREAMPIT"
    private val PREF_CARDS_JSON = "CARD_JSON"
    private lateinit var adapter: CardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var decor: RecyclerView.ItemDecoration
    private var columnCount: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recycler_view)
        cardList = ArrayList()
        adapter = CardAdapter(this, cardList)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val card = intent.getParcelableExtra<Card>(cardExtra)
                cardList.add(card)
                adapter.notifyDataSetChanged()
            }
        }, IntentFilter(eventDataSetChanged))

        calculateColumns()

        decor = GridSpacingItemDecoration(columnCount, dpToPx(10), true)

        recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        recyclerView.addItemDecoration(decor)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        prepareCards()
    }

    /**
     * If this is the first run (ergo, no preferences yet available), store the default cards in the SharedPreferences
     * Then the function retrieves the cards from prefs and loads them into the cardList
     */
    private fun prepareCards() {
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit().remove(PREF_CARDS_JSON).commit()
        if (!prefs.contains(PREF_CARDS_JSON)) {
            //First run
            val editor = prefs.edit()
            editor.putString(PREF_CARDS_JSON, "{\"cards\": [" +
                        "{" +
                            "\"name\": \"GO LIVE\"," +
                            "\"color\": \"#FF4B367C\"" +
                        "}," +
                        "{" +
                            "\"name\": \"TOGGLE REC\"," +
                            "\"color\": \"#FFD50000\"" +
                        "}," +
                        "{" +
                            "\"name\": \"DESKTOP AUDIO\"," +
                            "\"color\": \"#FF2E383F\"" +
                        "}" +
                    "]}")
            editor.commit()
        }
        val json = JSONObject(prefs.getString(PREF_CARDS_JSON, "{}"))

        val cards = json.getJSONArray("cards")
        for (i in 0 until cards.length()) {
            val card = cards.getJSONObject(i)
            cardList.add(Card(card))
        }

        adapter.notifyDataSetChanged()
    }

    /**
     * Calculate the amount of columns that should fit upon the screen given the size of the card (130dp)
     */
    private fun calculateColumns() {
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val singleWidth = dpToPx(130)
        columnCount = size.x / singleWidth
    }

    /**
     * Calculate the amount of pixels from dp
     */
    private fun dpToPx(dp: Int): Int {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_new_card -> {
                startActivity(Intent(this, NewCardActivity::class.java))
                true
            }
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Ensure the orientation can be changed without losing any data
     * This also improves performance, since the json does not need to be retrieved and loaded in
     */
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        calculateColumns()
        recyclerView.removeItemDecoration(decor)
        decor = GridSpacingItemDecoration(columnCount, dpToPx(10), true)
        recyclerView.addItemDecoration(decor)
        recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        adapter.notifyDataSetChanged()
    }
}
