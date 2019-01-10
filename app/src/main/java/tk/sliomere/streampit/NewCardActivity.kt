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

class NewCardActivity : AppCompatActivity(), ColorPickerDialogListener {

    lateinit var titleEditText: TextInputEditText
    lateinit var buttonIconImageBtn: ImageButton
    lateinit var colorImageButton: ImageButton
    var color: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_button)
        setSupportActionBar(toolbar)

        color = resources.getColor(R.color.cardPrimary, this.theme)
        titleEditText = findViewById(R.id.button_title_edit_text)
        buttonIconImageBtn = findViewById(R.id.button_icon_image_button)
        colorImageButton = findViewById(R.id.color_image_button)

        colorImageButton.setOnClickListener { v: View? ->
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
                val card = Card(titleEditText.text.toString(), color)
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
