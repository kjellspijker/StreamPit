package tk.sliomere.streampit

import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import tk.sliomere.streampit.cards.*
import tk.sliomere.streampit.websocket.StreamPitWebSocket
import java.net.URI

class MainActivity : AppCompatActivity() {

    companion object {
        var cardIDCounter = 0
        var removingCard = false
        const val firstRunExtra = "CARDEXTRA"
        const val authFailedExtra = "AUTHFAILEDEXTRA"
        const val cardExtra = "CARDEXTRA"
        const val cardIDExtra = "CARDID"
        const val eventDataSetChanged = "NOTIFYDATASETCHANGED"
        const val eventRemoveCard = "REMOVECARD"
        const val eventResetCards = "RESETCARDS"
        const val eventAuthFailed = "AUTHFAILED"
        const val eventReconnectWebSocket = "RECONNECTWEBSOCKET"
        const val PREF_NAME = "STREAMPIT"
        const val PREF_CARDS_JSON = "CARD_JSON"
        const val PREF_IP = "IPADDRESS"
        const val PREF_PORT = "PORTNUMBER"

        const val PREF_PASSWORD = "WEBSOCKETPASSWORD"
        const val requestCodeFirstSettings = 500
        const val requestCodeAuthFailed = 501

        lateinit var webSocketClient: StreamPitWebSocket
        lateinit var handler: Handler

        val listeningCards: HashMap<CardAction, ArrayList<Card>> = HashMap()
    }

    private var columnCount: Int = 2
    private lateinit var jsonCardList: JSONObject
    private lateinit var cardList: HashMap<Int, Card>
    private lateinit var idList: ArrayList<String>
    private lateinit var localBroadcastManager: LocalBroadcastManager
    private lateinit var adapter: CardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var decor: RecyclerView.ItemDecoration
    private lateinit var delMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        handler = Handler(Handler.Callback { msg: Message ->
            if (msg.obj is String) {
                Log.d("StreamPit", "Handler: " + msg.obj)
                if (msg.obj == eventAuthFailed) {
                    startActivityForResult(Intent(this, SettingsActivity::class.java).putExtra(authFailedExtra, true), MainActivity.requestCodeAuthFailed)
                } else if (msg.obj == eventReconnectWebSocket) {
                    webSocketClient.reconnect()
                    webSocketClient.changingPwd = false
                }
            }
            true
        })

        connectWebSocket()

        jsonCardList = JSONObject("{\"cards\": {}}")

        recyclerView = findViewById(R.id.recycler_view)
        cardList = HashMap()
        idList = ArrayList()
        adapter = CardAdapter(this, idList, cardList)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                /*
                 * Adding a new card to the list
                 */
                val card = intent.getParcelableExtra<Card>(cardExtra)
                idList.add(card.id)
                cardList[card.id.toInt()] = card
                adapter.notifyDataSetChanged()

                saveCards()
            }
        }, IntentFilter(eventDataSetChanged))

        localBroadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val cardID = intent.getStringExtra(MainActivity.cardIDExtra)
                idList.remove(cardID)
                cardList.remove(cardID.toInt())
                MainActivity.removingCard = false
                Toast.makeText(context, resources.getString(R.string.card_removed_toast), Toast.LENGTH_SHORT).show()
                delMenuItem.icon.setColorFilter(resources.getColor(android.R.color.white, theme), PorterDuff.Mode.SRC_ATOP)

                saveCards()
                adapter.notifyDataSetChanged()
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
            startActivityForResult(Intent(this, SettingsActivity::class.java).putExtra(firstRunExtra, true), MainActivity.requestCodeFirstSettings)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestCodeFirstSettings && resultCode ==  0) {
            Log.d("StreamPit", "First Time Setup Completed... Connecting...")
            connectWebSocket()
        }
    }

    private fun saveCards() {
        val cards = JSONObject()

        for (id in idList) {
            val card = cardList[id.toInt()]!!
            cards.put(card.id, card.toJSON())
        }

        jsonCardList.put("cards", cards)

        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_CARDS_JSON, jsonCardList.toString()).commit()
    }

    /**
     * If this is the first run (ergo, no preferences yet available), store the default cards in the SharedPreferences
     * Then the function retrieves the cards from prefs and loads them into the dialogCardList
     */
    private fun prepareCards() {
        idList.clear()
        cardList.clear()
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        prefs.edit().remove(PREF_CARDS_JSON).commit()
        if (!prefs.contains(PREF_CARDS_JSON)) {
//            First run
            val editor = prefs.edit()
            editor.putString(PREF_CARDS_JSON, "{\"cards\": {" +
                    "\"0\": {" +
                    "\"name\": \"GO LIVE\"," +
                    "\"cardAction\": \"TOGGLE_STREAMING\"," +
                    "\"icon\": \"" + resources.getResourceEntryName(R.drawable.icon_play) + "\"," +
                    "\"target\": \"\"," +
                    "\"color\": \"#FF4B367C\"" +
                    "}," +
                    "\"1\": {" +
                    "\"name\": \"TOGGLE REC\"," +
                    "\"cardAction\": \"TOGGLE_RECORDING\"," +
                    "\"icon\": \"" + resources.getResourceEntryName(R.drawable.icon_record_rec) + "\"," +
                    "\"target\": \"\"," +
                    "\"color\": \"#FFD50000\"" +
                    "}," +
                    "\"2\": {" +
                    "\"name\": \"DESKTOP AUDIO\"," +
                    "\"cardAction\": \"TOGGLE_MUTE\"," +
                    "\"icon\": \"" + resources.getResourceEntryName(R.drawable.icon_volume) + "\"," +
                    "\"target\": \"Desktop Audio\"," +
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
            val cardObject = when(CardAction.valueOf(card.getString("cardAction"))) {
                 CardAction.TOGGLE_MUTE -> ToggleMuteCard(id, card)
                 CardAction.TOGGLE_STREAMING -> ToggleStreamingCard(id, card)
                 CardAction.TOGGLE_VISIBILITY -> ToggleVisibilityCard(id, card)
                 CardAction.TOGGLE_RECORDING -> ToggleRecordingCard(id, card)
                 CardAction.SWITCH_SCENE -> SwitchSceneCard(id, card)
                 CardAction.NOTHING -> NothingCard(id, card)
            }
            Log.d("StreamPit", cardObject.icon)
            idList.add(cardObject.id)
            cardList[cardObject.id.toInt()] = cardObject
            if (cardObject.id.toInt() >= cardIDCounter) {
                cardIDCounter = cardObject.id.toInt() + 1
            }
        }

        adapter.notifyDataSetChanged()
        webSocketClient.onReady()
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

    fun setupIcons() {
        Log.d("StreamPit", "SetupIcons")
        for (id in cardList.keys) {
            cardList[id]?.reloadCard()
        }
    }

    fun connectWebSocket() {
        val pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val uri = URI("ws://" + pref.getString(PREF_IP, "") + ":" + pref.getInt(PREF_PORT, 4444))

        webSocketClient = StreamPitWebSocket(uri, pref.getString(PREF_PASSWORD, "")!!, ::setupIcons)
        webSocketClient.connect()
    }

}
