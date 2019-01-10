package tk.sliomere.streampit

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val PREF_NAME = "STREAMPIT"
    private val PREF_CARDS_JSON = "CARD_JSON"
    private lateinit var recyclerView: RecyclerView
    private lateinit var cardList: ArrayList<Card>
    private lateinit var adapter: CardAdapter
    private var columnCount: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recycler_view)
        cardList = ArrayList()
        adapter = CardAdapter(this, cardList)

        calculateColumns()

        recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(columnCount, dpToPx(10), true))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        prepareCards()
    }

    private fun prepareCards() {
        val prefs: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREF_CARDS_JSON).commit()
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

        Log.d("StreamPit", "\n" + cardList[0].toJSON().toString(4))

        adapter.notifyDataSetChanged()
    }

    private fun calculateColumns() {
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val singleWidth = dpToPx(130)
        Log.d("StreamPit", "" + size.x / singleWidth)
        columnCount = size.x / singleWidth
    }

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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
