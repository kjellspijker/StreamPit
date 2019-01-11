package tk.sliomere.streampit

import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    companion object {
        var cardIDCounter = 0
        var removingCard = false
        const val firstRunExtra = "CARDEXTRA"
        const val cardExtra = "CARDEXTRA"
        const val cardIDExtra = "CARDID"
        const val eventDataSetChanged = "NOTIFYDATASETCHANGED"
        const val eventRemoveCard = "REMOVECARD"
        const val eventResetCards = "RESETCARDS"
        const val PREF_NAME = "STREAMPIT"
        const val PREF_CARDS_JSON = "CARD_JSON"
        const val PREF_IP = "IPADDRESS"
        const val PREF_PORT = "PORTNUMBER"
    }

    private lateinit var jsonCardList: JSONObject
    private lateinit var cardList: ArrayList<Card>
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var adapter: CardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var decor: RecyclerView.ItemDecoration
    private var columnCount: Int = 2
    private lateinit var delMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        jsonCardList = JSONObject("{\"cards\": {}}")

        recyclerView = findViewById(R.id.recycler_view)
        cardList = ArrayList()
        adapter = CardAdapter(this, cardList)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                /*
                 * Adding a new card to the list
                 */
                val card = intent.getParcelableExtra<Card>(cardExtra)
                cardList.add(card)
                adapter.notifyDataSetChanged()

                saveCards()
            }
        }, IntentFilter(eventDataSetChanged))

        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val cardID = intent.getStringExtra(MainActivity.cardIDExtra)
                cardList.remove(cardList[cardID.toInt()])
                adapter.notifyDataSetChanged()
                MainActivity.removingCard = false
                Toast.makeText(context, resources.getString(R.string.card_removed_toast), Toast.LENGTH_SHORT).show()
                delMenuItem.icon.setColorFilter(resources.getColor(android.R.color.white, theme), PorterDuff.Mode.SRC_ATOP)

                saveCards()
            }
        }, IntentFilter(eventRemoveCard))

        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                prepareCards()
            }
        }, IntentFilter(eventResetCards))

        calculateColumns()

        decor = GridSpacingItemDecoration(columnCount, dpToPx(10), true)

        recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        recyclerView.addItemDecoration(decor)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        prepareCards()

        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(PREF_IP) && prefs.contains(PREF_PORT)) {
            //Start connection with OBS Remote
        } else {
            //No setup done or missing IP/PORT. Redirect to SettingsActivity for IP & Port of OBS Remote
            startActivity(Intent(this, SettingsActivity::class.java).putExtra(firstRunExtra, true))
        }
    }

    private fun saveCards() {
        val cards = jsonCardList.getJSONObject("cards")

        for (card in cardList) {
            cards.put(card.id, card.toJSON())
        }

        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_CARDS_JSON, jsonCardList.toString()).apply()
    }

    /**
     * If this is the first run (ergo, no preferences yet available), store the default cards in the SharedPreferences
     * Then the function retrieves the cards from prefs and loads them into the cardList
     */
    private fun prepareCards() {
        cardList.clear()
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit().remove(PREF_CARDS_JSON).commit()
        if (!prefs.contains(PREF_CARDS_JSON)) {
            //First run
            val editor = prefs.edit()
            editor.putString(PREF_CARDS_JSON, "{\"cards\": {" +
                        "\"0\": {" +
                            "\"name\": \"GO LIVE\"," +
                            "\"icon\": \"" + resources.getResourceEntryName(R.drawable.icon_play) + "\"," +
                            "\"color\": \"#FF4B367C\"" +
                        "}," +
                        "\"1\": {" +
                            "\"name\": \"TOGGLE REC\"," +
                            "\"icon\": \"" + resources.getResourceEntryName(R.drawable.icon_record_rec) + "\"," +
                            "\"color\": \"#FFD50000\"" +
                        "}," +
                        "\"2\": {" +
                            "\"name\": \"DESKTOP AUDIO\"," +
                            "\"icon\": \"" + resources.getResourceEntryName(R.drawable.icon_volume_high) + "\"," +
                            "\"color\": \"#FF2E383F\"" +
                        "}" +
                    "}}")
            editor.commit()
        }
        val json = JSONObject(prefs.getString(PREF_CARDS_JSON, "{}"))
        Log.d("StreamPit", json.toString(4))

        val cards = json.getJSONObject("cards")
        for (id in cards.keys()) {
            val card = cards.getJSONObject(id)
            val cardObject = Card(id, card)
            Log.d("StreamPit", cardObject.icon)
            cardList.add(cardObject)
            if (cardObject.id.toInt() >= cardIDCounter) {
                cardIDCounter = cardObject.id.toInt() + 1
            }
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

        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == R.id.action_del_card) {
                delMenuItem = menu.getItem(i)
                break
            }
        }

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
            R.id.action_del_card -> {
                if (!removingCard) {
                    Toast.makeText(this, resources.getString(R.string.removing_cards_toast), Toast.LENGTH_SHORT).show()
                    item.icon.setColorFilter(resources.getColor(R.color.colorRemovingCards, theme), PorterDuff.Mode.SRC_ATOP)
                    MainActivity.removingCard = true
                } else {
                    Toast.makeText(this, resources.getString(R.string.removing_cards_cancelled_toast), Toast.LENGTH_SHORT).show()
                    item.icon.setColorFilter(resources.getColor(android.R.color.white, theme), PorterDuff.Mode.SRC_ATOP)
                    MainActivity.removingCard = false
                }
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
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
