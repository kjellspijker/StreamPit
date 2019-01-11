package tk.sliomere.streampit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.textfield.TextInputEditText
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.android.synthetic.main.activity_new_button.*
import java.util.*
import kotlin.random.Random

class NewCardActivity : AppCompatActivity(), ColorPickerDialogListener {

    private lateinit var icon: String
    private lateinit var titleEditText: TextInputEditText
    private lateinit var buttonIconImageBtn: ImageButton
    private lateinit var colorImageButton: ImageButton
    private val availableIcons: ArrayList<String> = ArrayList()
    var color: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_button)
        setSupportActionBar(toolbar)

        for (field in R.drawable::class.java.fields) {
            if (field.name.startsWith("icon_")) {
                Log.d("StreamPit", field.name)
                availableIcons.add(field.name)
            }
        }

        icon = resources.getResourceEntryName(R.drawable.icon_play)
        color = resources.getColor(R.color.cardPrimary, this.theme)
        titleEditText = findViewById(R.id.button_title_edit_text)
        buttonIconImageBtn = findViewById(R.id.button_icon_image_button)
        colorImageButton = findViewById(R.id.color_image_button)

        buttonIconImageBtn.setOnClickListener{ _: View? ->
            icon = availableIcons[Random.nextInt(0, availableIcons.size)]
            Log.d("StreamPit", icon)
//            TODO("Implement choosing of the icons")
        }

        colorImageButton.setOnClickListener { _: View? ->
                ColorPickerDialog.newBuilder().setColor(resources.getColor(R.color.cardPrimary, this.theme)).show(this)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new_button, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_save -> {
                val card = Card((MainActivity.cardIDCounter++).toString(), titleEditText.text.toString(), color, icon)
//                MainActivity.addCard(card)
                val intent = Intent(MainActivity.eventDataSetChanged)
                intent.putExtra(MainActivity.cardExtra, card)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                Log.d("StreamPit", "Title: " + titleEditText.text.toString())
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogDismissed(dialogId: Int) {

    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        this.color = color
        Log.d("StreamPit", "Color set to: #" + Integer.toHexString(color))
        buttonIconImageBtn.setBackgroundColor(color)
        colorImageButton.setBackgroundColor(color)
    }

}
